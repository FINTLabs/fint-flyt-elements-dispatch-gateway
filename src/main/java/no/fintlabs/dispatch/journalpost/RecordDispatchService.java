package no.fintlabs.dispatch.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fintlabs.dispatch.file.FileDispatchService;
import no.fintlabs.dispatch.file.result.FileDispatchResult;
import no.fintlabs.dispatch.file.result.FilesDispatchResult;
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

import static java.util.stream.Collectors.toMap;
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
                            RecordDispatchResult.Status lastStatus = lastResult.getStatus();
                            List<RecordDispatchResult> successfulRecordDispatches = lastStatus == RecordDispatchResult.Status.ACCEPTED
                                    ? recordDispatchResults
                                    : recordDispatchResults.subList(0, recordDispatchResults.size() - 1);
                            return switch (lastStatus) {
                                case ACCEPTED -> RecordsDispatchResult.accepted(
                                        successfulRecordDispatches
                                );
                                case DECLINED, FILE_DECLINED -> RecordsDispatchResult.declined(
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
                    case DECLINED -> Mono.just(RecordDispatchResult.fileDeclined(filesDispatchResult));
                    case FAILED -> Mono.just(RecordDispatchResult.failed(filesDispatchResult));
                });
    }

    private Mono<RecordDispatchResult> addNewRecord(
            String caseId,
            JournalpostDto journalpostDto,
            FilesDispatchResult filesDispatchResult
    ) {
        Map<UUID, Link> archiveFileLinkPerFileUUID = filesDispatchResult.getSuccessfulFileDispatches()
                .stream()
                .collect(toMap(
                        FileDispatchResult::getFileId,
                        FileDispatchResult::getArchiveFileLink
                ));
        JournalpostWrapper journalpostWrapper = new JournalpostWrapper(
                journalpostMappingService.toJournalpostResource(
                        journalpostDto,
                        archiveFileLinkPerFileUUID
                )
        );
        return fintArchiveClient.postRecord(caseId, journalpostWrapper)
                .map(JournalpostResource::getJournalPostnummer)
                .map(journalPostNummer -> RecordDispatchResult.accepted(journalPostNummer, filesDispatchResult))
                .onErrorResume(
                        WebClientResponseException.class,
                        e -> Mono.just(RecordDispatchResult.declined(
                                e.getResponseBodyAsString(),
                                filesDispatchResult
                        ))
                ).onErrorResume(
                        e -> {
                            log.error("Failed to post record", e);
                            return Mono.just(RecordDispatchResult.failed(
                                    filesDispatchResult
                            ));
                        });
    }

}
