package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fint.model.resource.arkiv.noark.SaksmappeResource;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.mapping.JournalpostMappingService;
import no.fintlabs.mapping.SakMappingService;
import no.fintlabs.model.JournalpostWrapper;
import no.fintlabs.model.Result;
import no.fintlabs.model.instance.*;
import no.fintlabs.web.archive.FintArchiveClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class DispatchService {

    private final FileService fileClient;
    private final SakMappingService sakMappingService;
    private final JournalpostMappingService journalpostMappingService;
    private final FintArchiveClient fintArchiveClient;

    public DispatchService(
            FileService fileClient,
            SakMappingService sakMappingService,
            JournalpostMappingService journalpostMappingService,
            FintArchiveClient fintArchiveClient
    ) {
        this.fileClient = fileClient;
        this.sakMappingService = sakMappingService;
        this.journalpostMappingService = journalpostMappingService;
        this.fintArchiveClient = fintArchiveClient;
    }

    public Mono<Result> process(InstanceFlowHeaders instanceFlowHeaders, @Valid ArchiveInstance archiveInstance) {
        return getCaseId(archiveInstance.getSak())
                .flatMap(caseId -> addNewRecord(caseId, archiveInstance.getJournalpost()))
                .map(resultSakResource -> Result.accepted(resultSakResource.getMappeId().getIdentifikatorverdi()))
                .onErrorResume(WebClientResponseException.class, e ->
                        Mono.just(Result.declined(e.getResponseBodyAsString()))
                )
                .doOnError(e -> log.error("Failed to dispatch instance with headers=" + instanceFlowHeaders, e))
                .onErrorReturn(RuntimeException.class, Result.failed());
    }

    private Mono<String> getCaseId(SakDto sakDto) {
        return switch (sakDto.getType()) {
            case NEW -> createNewCase(sakDto.getNy())
                    .map(SaksmappeResource::getMappeId)
                    .map(Identifikator::getIdentifikatorverdi);
            case BY_ID -> Mono.just(sakDto.getId());
            case BY_SEARCH_OR_NEW -> getCaseBySearch(sakDto)
                    .flatMap(optionalCase -> optionalCase
                            .map(Mono::just)
                            .orElse(createNewCase(sakDto.getNy())))
                    .map(SaksmappeResource::getMappeId)
                    .map(Identifikator::getIdentifikatorverdi);
        };
    }

    private Mono<SakResource> createNewCase(NySakDto nySakDto) {
        SakResource sakResource = sakMappingService.toSakResource(nySakDto);
        return fintArchiveClient.postCase(sakResource)
                .doOnNext(result -> log.info("Created new case with id={}", result.getMappeId().getIdentifikatorverdi()));
    }

    private Mono<Optional<SakResource>> getCaseBySearch(SakDto sakDto) {
        throw new UnsupportedOperationException();
    }

    private Mono<SakResource> addNewRecord(String caseId, JournalpostDto journalpostDto) {
        return dispatchFiles(journalpostDto)
                .map(dokumentfilResourceLinkPerFileId ->
                        journalpostMappingService.toJournalpostResource(journalpostDto, dokumentfilResourceLinkPerFileId)
                )
                .doOnNext(journalpostResource -> log.info("Created record with number={}", journalpostResource.getJournalPostnummer()))
                .map(JournalpostWrapper::new)
                .flatMap(journalpostWrapper -> fintArchiveClient.putRecord(caseId, journalpostWrapper));
    }

    private Mono<Map<UUID, Link>> dispatchFiles(JournalpostDto journalpostDto) {
        return fileClient.dispatchFiles(
                journalpostDto
                        .getDokumentbeskrivelse()
                        .stream()
                        .map(DokumentbeskrivelseDto::getDokumentobjekt)
                        .flatMap(Collection::stream)
                        .map(DokumentobjektDto::getFileReference)
                        .toList()
        );
    }

}
