package no.fintlabs.flyt.gateway.application.archive.dispatch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.ArchiveInstance;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.JournalpostDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.sak.CaseDispatchService;
import no.fintlabs.flyt.gateway.application.archive.dispatch.sak.result.CaseDispatchResult;
import no.fintlabs.flyt.gateway.application.archive.kafka.error.InstanceDispatchingErrorProducerService;
import no.fintlabs.flyt.gateway.application.archive.resource.web.exceptions.KlasseOrderOutOfBoundsException;
import no.fintlabs.flyt.gateway.application.archive.resource.web.exceptions.SearchKlasseOrderNotFoundInCaseException;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class DispatchService {

    private final CaseDispatchService caseDispatchService;
    private final RecordsProcessingService recordsProcessingService;
    private final InstanceDispatchingErrorProducerService instanceDispatchingErrorProducerService;

    public DispatchService(
            CaseDispatchService caseDispatchService,
            RecordsProcessingService recordsProcessingService,
            InstanceDispatchingErrorProducerService instanceDispatchingErrorProducerService
    ) {
        this.caseDispatchService = caseDispatchService;
        this.recordsProcessingService = recordsProcessingService;
        this.instanceDispatchingErrorProducerService = instanceDispatchingErrorProducerService;
    }

    public Mono<DispatchResult> process(InstanceFlowHeaders instanceFlowHeaders, @Valid ArchiveInstance archiveInstance) {
        log.info("Dispatching instance with headers={}", instanceFlowHeaders);
        return (switch (archiveInstance.getType()) {
            case NEW -> processNew(archiveInstance);
            case BY_ID -> processById(archiveInstance);
            case BY_SEARCH_OR_NEW -> processBySearchOrNew(archiveInstance);
        })
                .doOnNext(dispatchResult -> logDispatchResult(instanceFlowHeaders, dispatchResult))
                .onErrorResume(e -> {
                    if (e instanceof SearchKlasseOrderNotFoundInCaseException) {
                        return handleDispatchError(instanceFlowHeaders, e, "SearchKlasseOrderNotFoundInCaseException encountered during dispatch", instanceDispatchingErrorProducerService);
                    } else if (e instanceof KlasseOrderOutOfBoundsException) {
                        return handleDispatchError(instanceFlowHeaders, e, "KlasseOrderOutOfBoundsException encountered during dispatch", instanceDispatchingErrorProducerService);
                    } else if (e instanceof NullPointerException) {
                        return handleDispatchError(instanceFlowHeaders, e, "NullPointerException encountered during dispatch", instanceDispatchingErrorProducerService);
                    } else {
                        return handleDispatchError(instanceFlowHeaders, e, "Unexpected exception encountered during dispatch", instanceDispatchingErrorProducerService);
                    }
                })
                .doOnError(e -> log.error("Failed to dispatch instance with headers={}", instanceFlowHeaders, e))
                .onErrorReturn(RuntimeException.class, DispatchResult.failed());
    }

    private void logDispatchResult(InstanceFlowHeaders instanceFlowHeaders, DispatchResult dispatchResult) {
        if (dispatchResult.getStatus() == DispatchStatus.ACCEPTED) {
            log.info("Successfully dispatched instance with headers={}", instanceFlowHeaders);
        } else if (dispatchResult.getStatus() == DispatchStatus.DECLINED) {
            log.info("Dispatch was declined for instance with headers={}", instanceFlowHeaders);
        } else if (dispatchResult.getStatus() == DispatchStatus.FAILED) {
            log.error("Failed to dispatch instance with headers={}", instanceFlowHeaders);
        }
    }

    private Mono<DispatchResult> processNew(ArchiveInstance archiveInstance) {
        return caseDispatchService.dispatch(archiveInstance.getNewCase())
                .flatMap(caseDispatchResult -> switch (caseDispatchResult.getStatus()) {
                            case ACCEPTED -> archiveInstance.getNewCase().getJournalpost()
                                    .map(journalpostDtos -> recordsProcessingService.processRecords(
                                                    caseDispatchResult.getArchiveCaseId(),
                                                    true,
                                                    journalpostDtos
                                            )
                                    ).orElse(Mono.just(DispatchResult.accepted(caseDispatchResult.getArchiveCaseId())));
                            case DECLINED -> Mono.just(DispatchResult.declined(
                                    "Sak was declined by the destination with message='" + caseDispatchResult.getErrorMessage() + "'"
                            ));
                            case FAILED -> Mono.just(DispatchResult.failed("Sak dispatch failed"));
                        }
                );
    }

    private Mono<DispatchResult> processById(ArchiveInstance archiveInstance) {
        return recordsProcessingService.processRecords(archiveInstance.getCaseId(), false, archiveInstance.getJournalpost());
    }

    @Getter
    @AllArgsConstructor
    private static class CaseInfo {
        private final boolean newCase;
        private final String caseId;
    }

    private Mono<DispatchResult> processBySearchOrNew(ArchiveInstance archiveInstance) {
        Optional<List<JournalpostDto>> journalpostDtosOptional = archiveInstance.getNewCase().getJournalpost();
        return caseDispatchService.findSingleCaseBySearch(archiveInstance)
                .flatMap(caseSearchResult -> {
                            if (caseSearchResult.getArchiveCaseIds().size() > 1) {
                                String caseIds = String.join(", ", caseSearchResult.getArchiveCaseIds());

                                return Mono.just(DispatchResult.declined("Found multiple cases: " + caseIds));
                            }

                            return (
                                    caseSearchResult.getArchiveCaseIds().size() == 1
                                            ? Mono.just(new CaseInfo(
                                            false,
                                            caseSearchResult.getArchiveCaseIds().get(0)))
                                            : caseDispatchService.dispatch(archiveInstance.getNewCase())
                                            .map(CaseDispatchResult::getArchiveCaseId)
                                            .map(caseId -> new CaseInfo(true, caseId))
                            ).flatMap(caseInfo ->
                                    journalpostDtosOptional
                                            .filter(journalpostDtos -> !journalpostDtos.isEmpty())
                                            .map(
                                                    journalpostDtos -> recordsProcessingService.processRecords(
                                                            caseInfo.getCaseId(),
                                                            caseInfo.isNewCase(),
                                                            journalpostDtos
                                                    )
                                            ).orElse(Mono.just(
                                                    DispatchResult.accepted(caseInfo.getCaseId())
                                            ))
                            );
                        }
                );
    }

    private Mono<DispatchResult> handleDispatchError(
            InstanceFlowHeaders instanceFlowHeaders,
            Throwable e,
            String logMessage,
            InstanceDispatchingErrorProducerService instanceDispatchingErrorProducerService
    ) {
        String errorMessage = (e != null && e.getMessage() != null) ? e.getMessage() : "Unknown error occurred";

        log.error("{}: {}", logMessage, errorMessage, e);

        instanceDispatchingErrorProducerService.publishGeneralSystemErrorEvent(
                instanceFlowHeaders,
                "An error occurred during dispatch: " + errorMessage
        );

        return Mono.error(Objects.requireNonNullElseGet(e, () -> new IllegalStateException("An unknown error occurred during dispatch")));
    }

}
