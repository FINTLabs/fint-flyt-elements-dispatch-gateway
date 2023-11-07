package no.fintlabs.dispatch.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fintlabs.dispatch.DispatchStatus;
import no.fintlabs.dispatch.file.FileDispatchService;
import no.fintlabs.dispatch.journalpost.result.RecordDispatchResult;
import no.fintlabs.dispatch.journalpost.result.RecordsDispatchResult;
import no.fintlabs.mapping.JournalpostMappingService;
import no.fintlabs.model.JournalpostWrapper;
import no.fintlabs.model.instance.JournalpostDto;
import no.fintlabs.web.archive.FintArchiveClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static no.fintlabs.dispatch.DispatchStatus.*;
import static no.fintlabs.dispatch.journalpost.result.RecordDispatchResult.Status.ACCEPTED;

@Slf4j
@Service
public class RecordDispatchService {

    private final JournalpostMappingService journalpostMappingService;
    private final FileDispatchService fileDispatchService;
    private final FintArchiveClient fintArchiveClient;

    public RecordDispatchService(
            JournalpostMappingService journalpostMappingService,
            FileDispatchService fileDispatchService,
            FintArchiveClient fintArchiveClient
    ) {
        this.journalpostMappingService = journalpostMappingService;
        this.fileDispatchService = fileDispatchService;
        this.fintArchiveClient = fintArchiveClient;
    }

    public Mono<RecordsDispatchResult> dispatch(String caseId, List<JournalpostDto> journalpostDtos) {
        if (journalpostDtos.isEmpty()) {
            return Mono.just(RecordsDispatchResult.accepted(Collections.emptyList()));
        }
        return Flux.fromIterable(journalpostDtos)
                .doOnNext(journalpostDto -> log.info("Dispatching record"))
                .concatMap(journalpostDto -> addFilesAndNewRecord(caseId, journalpostDto))
                .doOnNext(this::logRecordDispatchResult)
                .takeWhile(recordDispatchResult -> recordDispatchResult.getStatus() == ACCEPTED)
                .collectList()
                .map(recordDispatchResults -> {
                            RecordDispatchResult lastResult = recordDispatchResults.get(recordDispatchResults.size() - 1);
                            DispatchStatus lastStatus = lastResult.getStatus();
                            List<RecordDispatchResult> successfulRecordDispatches = lastStatus == ACCEPTED
                                    ? recordDispatchResults
                                    : recordDispatchResults.subList(0, recordDispatchResults.size() - 1);
                            return switch (lastStatus) {
                                case ACCEPTED -> RecordsDispatchResult.accepted(
                                        successfulRecordDispatches
                                );
                                case DECLINED -> RecordsDispatchResult.declined(
                                        successfulRecordDispatches,
                                        lastResult
                                );
                                case FAILED -> RecordsDispatchResult.failed(
                                        successfulRecordDispatches,
                                        lastResult
                                );
                            };
                        }
                ).doOnError(e -> log.error("Instance dispatch failed", e))
                .onErrorResume(e -> Mono.just(
                        RecordsDispatchResult.failed(Collections.emptyList(), null))
                );
    }

    private void logRecordDispatchResult(RecordDispatchResult recordDispatchResult) {
        if (recordDispatchResult.getStatus() == RecordDispatchResult.Status.ACCEPTED) {
            log.info("Successfully dispatched record");
        } else if (recordDispatchResult.getStatus() == RecordDispatchResult.Status.DECLINED) {
            log.info("Record dispatch was declined with message='{}'", recordDispatchResult.getErrorMessage());
        } else if (recordDispatchResult.getStatus() == RecordDispatchResult.Status.FILE_DECLINED) {
            log.info("Record dispatch was declined because file dispatch was declined");
        } else if (recordDispatchResult.getStatus() == RecordDispatchResult.Status.FAILED) {
            log.info("Record dispatch failed");
        }
    }

    private Mono<RecordDispatchResult> addFilesAndNewRecord(String caseId, JournalpostDto journalpostDto) {
        return fileDispatchService.dispatchFiles(
                        journalpostDto.getDokumentbeskrivelse().orElse(Collections.emptyList())
                )
                .flatMap(filesDispatchResult -> switch (filesDispatchResult.getStatus()) {
                    case ACCEPTED -> addNewRecord(caseId, journalpostDto, filesDispatchResult);
                    case DECLINED -> Mono.just(
                            RecordDispatchResult.declined(
                                    filesDispatchResult.getErrorMessage(), filesDispatchResult.getFunctionalWarningMessage()
                            )
                    );
                    case FAILED -> Mono.just(
                            RecordDispatchResult.failed(
                                    filesDispatchResult.getErrorMessage(), filesDispatchResult.getFunctionalWarningMessage()
                            )
                    );
                });
    }

    private Mono<RecordDispatchResult> addNewRecord(
            String caseId,
            JournalpostDto journalpostDto,
            Map<UUID, Link> archiveFileLinkPerFileId
    ) {
        JournalpostWrapper journalpostWrapper = new JournalpostWrapper(
                journalpostMappingService.toJournalpostResource(
                        journalpostDto,
                        archiveFileLinkPerFileId
                )
        );
        return fintArchiveClient.postRecord(caseId, journalpostWrapper)
                .map(JournalpostResource::getJournalPostnummer)
                .map(RecordDispatchResult::accepted)
                .onErrorResume(
                        WebClientResponseException.class,
                        e -> fileDispatchService.createFunctionalWarningMessage(archiveFileLinkPerFileId.values())
                                .map(filesFunctionalWarningOptional -> RecordDispatchResult.declined(
                                        e.getResponseBodyAsString(),
                                        filesFunctionalWarningOptional.orElse(null)
                                ))

                )
                .onErrorResume(
                        e -> {
                            log.error("Failed to post record", e);
                            return fileDispatchService.createFunctionalWarningMessage(archiveFileLinkPerFileId.values())
                                    .map(filesFunctionalWarningOptional -> RecordDispatchResult.failed(
                                            null,
                                            filesFunctionalWarningOptional.orElse(null)
                                    ));
                        }
                );
    }

    private List<Long> getJournalpostNumbers(List<RecordDispatchResult> recordDispatchResults) {
        return recordDispatchResults.stream().map(RecordDispatchResult::getJournalpostId).collect(Collectors.toList());
    }

}
