package no.fintlabs.flyt.gateway.application.archive.dispatch.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fintlabs.flyt.gateway.application.archive.dispatch.file.FilesDispatchService;
import no.fintlabs.flyt.gateway.application.archive.dispatch.journalpost.result.RecordDispatchResult;
import no.fintlabs.flyt.gateway.application.archive.dispatch.mapping.JournalpostMappingService;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.DokumentbeskrivelseDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.DokumentobjektDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.JournalpostDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.web.FintArchiveDispatchClient;
import no.fintlabs.flyt.gateway.application.archive.resource.web.FintArchiveResourceClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Service
public class RecordDispatchService {

    private final JournalpostMappingService journalpostMappingService;
    private final FilesDispatchService filesDispatchService;
    private final FintArchiveResourceClient fintArchiveResourceClient;

    private final FintArchiveDispatchClient fintArchiveDispatchClient;

    public RecordDispatchService(
            JournalpostMappingService journalpostMappingService,
            FilesDispatchService filesDispatchService,
            FintArchiveResourceClient fintArchiveResourceClient,
            FintArchiveDispatchClient fintArchiveDispatchClient
    ) {
        this.journalpostMappingService = journalpostMappingService;
        this.filesDispatchService = filesDispatchService;
        this.fintArchiveResourceClient = fintArchiveResourceClient;
        this.fintArchiveDispatchClient = fintArchiveDispatchClient;
    }

    public Mono<RecordDispatchResult> dispatch(String caseId, JournalpostDto journalpostDto) {
        log.info("Dispatching record");
        return findDuplicateRecordId(caseId, journalpostDto)
                .flatMap(duplicateRecordIdOptional -> duplicateRecordIdOptional
                        .map(duplicateRecordId -> {
                            log.info("Found duplicate record with id{} in case with id={}", duplicateRecordId, caseId);
                            return Mono.just(RecordDispatchResult.accepted(duplicateRecordId));
                        })
                        .orElseGet(() -> dispatchNewJournalpost(caseId, journalpostDto))
                );
    }

    private Mono<Optional<Long>> findDuplicateRecordId(String caseId, JournalpostDto journalpostDto) {
        if (journalpostDto.getTittel().isEmpty()) {
            return Mono.just(Optional.empty());
        }
        String tittel = journalpostDto.getTittel().get();
        return fintArchiveResourceClient.findCase(caseId)
                .map(sak -> sak.flatMap(sakResource -> sakResource.getJournalpost()
                        .stream()
                        .filter(existingJournalpost -> existingJournalpost.getTittel().equals(tittel))
                        .findFirst()
                        .map(JournalpostResource::getJournalPostnummer)
                ));
    }

    private Mono<RecordDispatchResult> dispatchNewJournalpost(String caseId, JournalpostDto journalpostDto) {
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
                                            filesDispatchResult.getErrorMessage() + "'"
                            )
                    );
                    case FAILED -> Mono.just(
                            RecordDispatchResult.failed("Dokumentobjekt dispatch failed")
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
        return fintArchiveDispatchClient.postRecord(caseId, journalpostResource)
                .map(JournalpostResource::getJournalPostnummer)
                .map(RecordDispatchResult::accepted)
                .onErrorResume(
                        WebClientResponseException.class,
                        e -> Mono.just(RecordDispatchResult.declined(e.getResponseBodyAsString()))
                )
                .onErrorResume(
                        e -> {
                            log.error("Failed to post record", e);
                            return Mono.just(RecordDispatchResult.failed(null));
                        }
                );
    }

}
