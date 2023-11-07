package no.fintlabs.dispatch;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentfilResource;
import no.fintlabs.dispatch.file.result.FileDispatchResult;
import no.fintlabs.dispatch.journalpost.RecordDispatchService;
import no.fintlabs.dispatch.journalpost.result.RecordDispatchResult;
import no.fintlabs.dispatch.journalpost.result.RecordsDispatchResult;
import no.fintlabs.dispatch.sak.CaseDispatchService;
import no.fintlabs.dispatch.sak.result.CaseDispatchResult;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.model.instance.ArchiveInstance;
import no.fintlabs.model.instance.JournalpostDto;
import no.fintlabs.web.archive.FintArchiveClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.validation.Valid;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Slf4j
@Service
public class DispatchService {

    private final CaseDispatchService caseDispatchService;
    private final RecordDispatchService recordDispatchService;
    private final FintArchiveClient fintArchiveClient;


    public DispatchService(
            CaseDispatchService caseDispatchService,
            RecordDispatchService recordDispatchService,
            FintArchiveClient fintArchiveClient) {
        this.caseDispatchService = caseDispatchService;
        this.recordDispatchService = recordDispatchService;
        this.fintArchiveClient = fintArchiveClient;
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
        if (dispatchResult.getStatus() == DispatchResult.Status.ACCEPTED) {
            log.info("Successfully dispatched instance with headers=" + instanceFlowHeaders);
        } else if (dispatchResult.getStatus() == DispatchResult.Status.DECLINED) {
            log.info("Dispatch was declined for instance with headers=" + instanceFlowHeaders);
        } else if (dispatchResult.getStatus() == DispatchResult.Status.FAILED) {
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

    // TODO eivindmorch 03/11/2023 : Fjerne retries?

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
        return recordDispatchService.dispatch(
                archiveCaseId,
                journalpostDtos
        ).flatMap(recordsDispatchResult ->
                switch (recordsDispatchResult.getStatus()) {
                    case ACCEPTED -> Mono.just(DispatchResult.accepted(
                            formatCaseIdAndJournalpostIds(
                                    archiveCaseId,
                                    getJournalpostNumbers(recordsDispatchResult.getSuccessfulRecordDispatchResults())
                            )
                    ));
                    case DECLINED -> createArchiveCleanupRequiredWarningMessage(archiveCaseId, newCase, recordsDispatchResult)
                            .map(archiveCleanupRequiredWarningMessage ->
                                    DispatchResult.declined(
                                            switch (recordsDispatchResult.getFailedRecordDispatchResult().getStatus()) {
                                                case DECLINED -> "Journalpost was declined by the destination." +
                                                        archiveCleanupRequiredWarningMessage +
                                                        " Error message from destination: '" +
                                                        recordsDispatchResult.getFailedRecordDispatchResult().getErrorMessage() +
                                                        "'";
                                                case FILE_DECLINED -> "File was declined by the destination. Error message from destination: '" +
                                                        archiveCleanupRequiredWarningMessage +
                                                        recordsDispatchResult.getFailedRecordDispatchResult().getFilesDispatchResult().getFailedFileDispatch().getErrorMessage() +
                                                        "'";
                                                default -> throw new IllegalStateException(); // TODO eivindmorch 31/10/2023 : Fix?
                                            }
                                    )
                            );
                    case FAILED -> Mono.just(DispatchResult.failed("Journalpost dispatch failed." +
                            createArchiveCleanupRequiredWarningMessage(archiveCaseId, newCase, recordsDispatchResult)
                    ));
                }
        );
    }

    private String formatCaseIdAndJournalpostIds(String caseId, List<Long> journalpostNumbers) {
        return caseId + journalpostNumbers
                .stream()
                .map(Object::toString)
                .collect(joining(",", "-[", "]"));
    }

    private List<Long> getJournalpostNumbers(List<RecordDispatchResult> recordDispatchResults) {
        return recordDispatchResults.stream().map(RecordDispatchResult::getJournalpostId).collect(Collectors.toList());
    }

    private Mono<String> createArchiveCleanupRequiredWarningMessage(
            String archiveCaseId,
            boolean newCase,
            RecordsDispatchResult recordsDispatchResult
    ) {
        if (!hasDispatchedElementsToArchiveThatNeedToBeCleanedUp(newCase, recordsDispatchResult)) {
            return Mono.just("");
        }
        List<FileDispatchResult> successfulFileDispatchesForFailedRecord = recordsDispatchResult.getFailedRecordDispatchResult().getFilesDispatchResult().getSuccessfulFileDispatches();
        List<Long> successfullJournalpostIds = recordsDispatchResult.getSuccessfulRecordDispatchResults()
                .stream()
                .map(RecordDispatchResult::getJournalpostId)
                .toList();

        return (
                successfulFileDispatchesForFailedRecord.isEmpty() ? Mono.just(List.<String>of()) :
                        getFileIds(
                                successfulFileDispatchesForFailedRecord
                                        .stream()
                                        .map(FileDispatchResult::getArchiveFileLink)
                                        .toList()
                        ).retryWhen(Retry.backoff(5, Duration.ofSeconds(3)))
                                .doOnError(
                                        e -> log.error("Could not get file ids for files with links=" +
                                                successfulFileDispatchesForFailedRecord
                                                        .stream()
                                                        .map(FileDispatchResult::getArchiveFileLink)
                                                        .map(Link::getHref)
                                                        .collect(joining(",", "[", "]"))
                                        ))
                                .onErrorResume(e -> Mono.just(successfulFileDispatchesForFailedRecord
                                        .stream()
                                        .map(FileDispatchResult::getArchiveFileLink)
                                        .map(Link::getHref)
                                        .toList())
                                )
        ).map(fileIds -> {
            StringJoiner stringJoiner = new StringJoiner(", ", " !!!Already successfully dispatched ", "!!!");
            if (newCase) {
                stringJoiner.add("sak with id=" + archiveCaseId);
            }
            if (successfullJournalpostIds.size() == 1) {
                stringJoiner.add("journalpost with id=" + successfullJournalpostIds.get(0));
            } else if (!successfullJournalpostIds.isEmpty()) {
                stringJoiner.add("journalposts with ids=" + successfullJournalpostIds.stream().map(String::valueOf).collect(joining(",", "[", "]")));
            }
            if (fileIds.size() == 1) {
                stringJoiner.add("dokumentobjekt with id=" + fileIds.get(0));
            } else if (!fileIds.isEmpty()) {
                stringJoiner.add("dokumentobjekts with ids=" + fileIds.stream().collect(joining(",", "[", "]")));
            }
            return stringJoiner.toString();
        });
    }

    private boolean hasDispatchedElementsToArchiveThatNeedToBeCleanedUp(
            boolean newCase,
            RecordsDispatchResult recordsDispatchResult
    ) {
        return newCase ||
                !recordsDispatchResult.getSuccessfulRecordDispatchResults().isEmpty() ||
                !recordsDispatchResult.getFailedRecordDispatchResult().getFilesDispatchResult().getSuccessfulFileDispatches().isEmpty();
    }

    public Mono<List<String>> getFileIds(Collection<Link> fileLinks) {
        return Flux.fromIterable(fileLinks)
                .concatMap(fintArchiveClient::getFile)
                .map(DokumentfilResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .collectList();
    }

}
