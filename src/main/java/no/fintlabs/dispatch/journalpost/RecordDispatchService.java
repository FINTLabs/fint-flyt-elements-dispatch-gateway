package no.fintlabs.dispatch.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fintlabs.dispatch.file.FilesDispatchService;
import no.fintlabs.dispatch.file.FilesWarningMessageService;
import no.fintlabs.dispatch.journalpost.result.RecordDispatchResult;
import no.fintlabs.mapping.JournalpostMappingService;
import no.fintlabs.model.instance.DokumentbeskrivelseDto;
import no.fintlabs.model.instance.DokumentobjektDto;
import no.fintlabs.model.instance.JournalpostDto;
import no.fintlabs.web.archive.FintArchiveClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Service
public class RecordDispatchService {

    private final JournalpostMappingService journalpostMappingService;
    private final FilesDispatchService filesDispatchService;

    private final FilesWarningMessageService filesWarningMessageService;
    private final FintArchiveClient fintArchiveClient;

    public RecordDispatchService(
            JournalpostMappingService journalpostMappingService,
            FilesDispatchService filesDispatchService,
            FilesWarningMessageService filesWarningMessageService,
            FintArchiveClient fintArchiveClient
    ) {
        this.journalpostMappingService = journalpostMappingService;
        this.filesDispatchService = filesDispatchService;
        this.filesWarningMessageService = filesWarningMessageService;
        this.fintArchiveClient = fintArchiveClient;
    }

    public Mono<RecordDispatchResult> dispatch(String caseId, JournalpostDto journalpostDto) {
        log.info("Dispatching record");
        List<DokumentobjektDto> dokumentobjektDtos = journalpostDto.getDokumentbeskrivelse()
                .map(this::getDokumentObjektDtos)
                .orElse(List.of());
        if (dokumentobjektDtos.isEmpty()) {
            return dispatch(caseId, journalpostDto, Map.of());
        }
        return filesDispatchService.dispatch(dokumentobjektDtos)
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

    private List<DokumentobjektDto> getDokumentObjektDtos(Collection<DokumentbeskrivelseDto> dokumentbeskrivelseDtos) {
        return dokumentbeskrivelseDtos
                .stream()
                .map(DokumentbeskrivelseDto::getDokumentobjekt)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(Collection::stream)
                .toList();
    }

    private Mono<RecordDispatchResult> dispatch(
            String caseId,
            JournalpostDto journalpostDto,
            Map<UUID, Link> archiveFileLinkPerFileId
    ) {
        JournalpostResource journalpostResource = journalpostMappingService.toJournalpostResource(
                journalpostDto,
                archiveFileLinkPerFileId
        );
        return fintArchiveClient.postRecord(caseId, journalpostResource)
                .map(JournalpostResource::getJournalPostnummer)
                .map(RecordDispatchResult::accepted)
                .onErrorResume(
                        WebClientResponseException.class,
                        e -> filesWarningMessageService.createFunctionalWarningMessage(archiveFileLinkPerFileId.values())
                                .map(filesFunctionalWarningOptional -> RecordDispatchResult.declined(
                                        e.getResponseBodyAsString(),
                                        filesFunctionalWarningOptional.orElse(null)
                                ))

                )
                .onErrorResume(
                        e -> {
                            log.error("Failed to post record", e);
                            return filesWarningMessageService.createFunctionalWarningMessage(archiveFileLinkPerFileId.values())
                                    .map(filesFunctionalWarningOptional -> RecordDispatchResult.failed(
                                            null,
                                            filesFunctionalWarningOptional.orElse(null)
                                    ));
                        }
                );
    }

}
