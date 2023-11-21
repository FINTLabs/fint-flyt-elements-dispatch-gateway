package no.fintlabs.dispatch;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.dispatch.journalpost.RecordsDispatchService;
import no.fintlabs.dispatch.sak.CaseDispatchService;
import no.fintlabs.dispatch.sak.result.CaseDispatchResult;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.model.instance.ArchiveInstance;
import no.fintlabs.model.instance.JournalpostDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;

@Slf4j
@Service
public class DispatchService {

    private final CaseDispatchService caseDispatchService;
    private final RecordsDispatchService recordsDispatchService;

    public DispatchService(
            CaseDispatchService caseDispatchService,
            RecordsDispatchService recordsDispatchService
    ) {
        this.caseDispatchService = caseDispatchService;
        this.recordsDispatchService = recordsDispatchService;
    }

    public Mono<DispatchResult> process(InstanceFlowHeaders instanceFlowHeaders, @Valid ArchiveInstance archiveInstance) {
        log.info("Dispatching instance with headers=" + instanceFlowHeaders);
        return (switch (archiveInstance.getType()) {
            case NEW -> processNew(archiveInstance);
            case BY_ID -> processById(archiveInstance);
            case BY_SEARCH_OR_NEW -> processBySearchOrNew(archiveInstance);
        })
                .doOnNext(dispatchResult -> logDispatchResult(instanceFlowHeaders, dispatchResult))
                .doOnError(e -> log.error("Failed to dispatch instance with headers=" + instanceFlowHeaders, e))
                .onErrorReturn(RuntimeException.class, DispatchResult.failed());
    }

    private void logDispatchResult(InstanceFlowHeaders instanceFlowHeaders, DispatchResult dispatchResult) {
        if (dispatchResult.getStatus() == DispatchStatus.ACCEPTED) {
            log.info("Successfully dispatched instance with headers=" + instanceFlowHeaders);
        } else if (dispatchResult.getStatus() == DispatchStatus.DECLINED) {
            log.info("Dispatch was declined for instance with headers=" + instanceFlowHeaders);
        } else if (dispatchResult.getStatus() == DispatchStatus.FAILED) {
            log.error("Failed to dispatch instance with headers=" + instanceFlowHeaders);
        }
    }

    private Mono<DispatchResult> processNew(ArchiveInstance archiveInstance) {
        return caseDispatchService.dispatch(archiveInstance.getNewCase())
                .flatMap(caseDispatchResult -> switch (caseDispatchResult.getStatus()) {
                            case ACCEPTED -> archiveInstance.getNewCase().getJournalpost()
                                    .map(journalpostDtos -> processRecords(
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
        return processRecords(archiveInstance.getCaseId(), false, archiveInstance.getJournalpost());
    }

    private Mono<DispatchResult> processBySearchOrNew(ArchiveInstance archiveInstance) {
        Optional<List<JournalpostDto>> journalpostDtosOptional = archiveInstance.getNewCase().getJournalpost();
        if (journalpostDtosOptional.isEmpty() || journalpostDtosOptional.get().isEmpty()
        ) {
            return Mono.just(DispatchResult.declined("Instance contains no records"));
        }
        return caseDispatchService.findCasesBySearch(archiveInstance)
                .flatMap(cases -> {
                            if (cases.size() > 1) {
                                return Mono.just(DispatchResult.declined("Found multiple cases"));
                            }
                            return cases.size() == 1
                                    ? processRecords(
                                    cases.get(0).getMappeId().getIdentifikatorverdi(),
                                    false,
                                    journalpostDtosOptional.get()
                            )
                                    : caseDispatchService.dispatch(archiveInstance.getNewCase())
                                    .map(CaseDispatchResult::getArchiveCaseId)
                                    .flatMap(caseId -> processRecords(
                                            caseId,
                                            true,
                                            journalpostDtosOptional.get()
                                    ));
                        }
                );
    }

    private Mono<DispatchResult> processRecords(String archiveCaseId, boolean newCase, List<JournalpostDto> journalpostDtos) {
        return recordsDispatchService.dispatch(
                archiveCaseId,
                journalpostDtos
        ).map(recordsDispatchResult ->
                switch (recordsDispatchResult.getStatus()) {
                    case ACCEPTED -> DispatchResult.accepted(
                            formatCaseIdAndJournalpostIds(
                                    archiveCaseId,
                                    recordsDispatchResult.getJournalpostIds()
                            )
                    );
                    case DECLINED -> DispatchResult.declined(
                            "Journalpost was declined by the destination." +
                                    combineFunctionalWarningMessages(
                                            archiveCaseId,
                                            newCase,
                                            recordsDispatchResult.getFunctionalWarningMessages()
                                    ) +
                                    " Error message from destination: '" +
                                    recordsDispatchResult.getErrorMessage() +
                                    "'");
                    case FAILED -> DispatchResult.failed("Journalpost dispatch failed." +
                            combineFunctionalWarningMessages(
                                    archiveCaseId,
                                    newCase,
                                    recordsDispatchResult.getFunctionalWarningMessages()
                            )
                    );
                }
        );
    }

    private String formatCaseIdAndJournalpostIds(String caseId, List<Long> journalpostNumbers) {
        return caseId + journalpostNumbers
                .stream()
                .map(Object::toString)
                .collect(joining(",", "-[", "]"));
    }

    private String combineFunctionalWarningMessages(
            String archiveCaseId,
            boolean newCase,
            List<String> functionalWarningMessages
    ) {
        StringJoiner stringJoiner = new StringJoiner(", ", " !!!Already successfully dispatched ", "!!!");
        if (newCase) {
            stringJoiner.add("sak with id=" + archiveCaseId);
        }
        functionalWarningMessages.forEach(stringJoiner::add);

        return stringJoiner.toString();
    }

}
