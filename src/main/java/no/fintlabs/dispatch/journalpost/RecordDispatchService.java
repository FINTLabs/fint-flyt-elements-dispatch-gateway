package no.fintlabs.dispatch.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fintlabs.dispatch.file.FilesDispatchService;
import no.fintlabs.dispatch.journalpost.result.RecordDispatchResult;
import no.fintlabs.mapping.JournalpostMappingService;
import no.fintlabs.model.JournalpostWrapper;
import no.fintlabs.model.instance.JournalpostDto;
import no.fintlabs.web.archive.FintArchiveClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class RecordDispatchService {

    private final JournalpostMappingService journalpostMappingService;
    private final FilesDispatchService filesDispatchService;
    private final FintArchiveClient fintArchiveClient;

    public RecordDispatchService(
            JournalpostMappingService journalpostMappingService,
            FilesDispatchService filesDispatchService,
            FintArchiveClient fintArchiveClient
    ) {
        this.journalpostMappingService = journalpostMappingService;
        this.filesDispatchService = filesDispatchService;
        this.fintArchiveClient = fintArchiveClient;
    }

    public Mono<RecordDispatchResult> dispatch(String caseId, JournalpostDto journalpostDto) {
        log.info("Dispatching record");
        return filesDispatchService.dispatch(
                        journalpostDto.getDokumentbeskrivelse().orElse(Collections.emptyList())
                )
                .flatMap(filesDispatchResult -> switch (filesDispatchResult.getStatus()) {
                    case ACCEPTED ->
                            dispatch(caseId, journalpostDto, filesDispatchResult.getArchiveFileLinkPerFileId());
                    case DECLINED -> Mono.just(
                            RecordDispatchResult.declined(
                                    "Dokumentobjekt declined by destination with message='" +
                                            filesDispatchResult.getErrorMessage() + "'",
                                    filesDispatchResult.getFunctionalWarningMessage().orElse(null)
                            )
                    );
                    case FAILED -> Mono.just(
                            RecordDispatchResult.failed(
                                    "Dokumentobjekt dispatch failed",
                                    filesDispatchResult.getFunctionalWarningMessage().orElse(null)
                            )
                    );
                }).doOnNext(result -> log.info("Dispatch result=" + result.toString()));
    }

    private Mono<RecordDispatchResult> dispatch(
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
                        e -> filesDispatchService.createFunctionalWarningMessage(archiveFileLinkPerFileId.values())
                                .map(filesFunctionalWarningOptional -> RecordDispatchResult.declined(
                                        e.getResponseBodyAsString(),
                                        filesFunctionalWarningOptional.orElse(null)
                                ))

                )
                .onErrorResume(
                        e -> {
                            log.error("Failed to post record", e);
                            return filesDispatchService.createFunctionalWarningMessage(archiveFileLinkPerFileId.values())
                                    .map(filesFunctionalWarningOptional -> RecordDispatchResult.failed(
                                            null,
                                            filesFunctionalWarningOptional.orElse(null)
                                    ));
                        }
                );
    }

}
