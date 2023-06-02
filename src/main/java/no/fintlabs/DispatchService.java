package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fint.model.resource.arkiv.noark.SaksmappeResource;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.mapping.JournalpostMappingService;
import no.fintlabs.mapping.SakMappingService;
import no.fintlabs.model.CaseDispatchType;
import no.fintlabs.model.JournalpostWrapper;
import no.fintlabs.model.Result;
import no.fintlabs.model.instance.*;
import no.fintlabs.web.archive.CaseSearchParametersService;
import no.fintlabs.web.archive.FintArchiveClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DispatchService {

    private final FileService fileClient;
    private final SakMappingService sakMappingService;
    private final JournalpostMappingService journalpostMappingService;
    private final CaseSearchParametersService caseSearchParametersService;
    private final FintArchiveClient fintArchiveClient;

    public DispatchService(
            FileService fileClient,
            SakMappingService sakMappingService,
            JournalpostMappingService journalpostMappingService,
            CaseSearchParametersService caseSearchParametersService, FintArchiveClient fintArchiveClient
    ) {
        this.fileClient = fileClient;
        this.sakMappingService = sakMappingService;
        this.journalpostMappingService = journalpostMappingService;
        this.caseSearchParametersService = caseSearchParametersService;
        this.fintArchiveClient = fintArchiveClient;
    }

    public Mono<Result> process(InstanceFlowHeaders instanceFlowHeaders, @Valid ArchiveInstance archiveInstance) {
        return getCaseId(archiveInstance)
                .flatMap(caseId ->
                        archiveInstance.getType() == CaseDispatchType.NEW

                                ? archiveInstance.getNewCase().getJournalpost()
                                .map(journalpostDtos -> addNewRecords(caseId, journalpostDtos)
                                        .map(journalpostNummer -> caseId)
                                )
                                .orElse(Mono.just(caseId))

                                : addNewRecords(caseId, archiveInstance.getJournalpost())
                                .map(journalpostNummers -> caseId + journalpostNummers
                                        .stream()
                                        .map(Object::toString)
                                        .collect(Collectors.joining(",", "-[", "]"))
                                )
                )
                .map(Result::accepted)
                .onErrorResume(WebClientResponseException.class, e ->
                        Mono.just(Result.declined(e.getResponseBodyAsString()))
                )
                .doOnError(e -> log.error("Failed to dispatch instance with headers=" + instanceFlowHeaders, e))
                .onErrorReturn(RuntimeException.class, Result.failed());
    }

    private Mono<String> getCaseId(ArchiveInstance archiveInstance) {
        return switch (archiveInstance.getType()) {
            case NEW -> createNewCase(archiveInstance.getNewCase())
                    .map(SaksmappeResource::getMappeId)
                    .map(Identifikator::getIdentifikatorverdi);
            case BY_ID -> Mono.just(archiveInstance.getCaseId());
            case BY_SEARCH_OR_NEW -> findCaseBySearch(archiveInstance)
                    .flatMap(optionalCase -> optionalCase
                            .map(Mono::just)
                            .orElse(createNewCase(archiveInstance.getNewCase())))
                    .map(SaksmappeResource::getMappeId)
                    .map(Identifikator::getIdentifikatorverdi);
        };
    }

    private Mono<SakResource> createNewCase(SakDto sakDto) {
        SakResource sakResource = sakMappingService.toSakResource(sakDto);
        log.debug("Creating new case: {}", sakResource);
        return fintArchiveClient.postCase(sakResource)
                .doOnNext(result -> log.info("Created new case with id={}", result.getMappeId().getIdentifikatorverdi()));
    }

    private Mono<Optional<SakResource>> findCaseBySearch(ArchiveInstance archiveInstance) {
        String caseFilter = caseSearchParametersService.createFilterQueryParamValue(
                archiveInstance.getNewCase(),
                archiveInstance.getCaseSearchParameters()
        );
        return fintArchiveClient.findCasesWithFilter(caseFilter)
                .map(cases -> {
                    if (cases.size() == 1) {
                        return Optional.of(cases.get(0));
                    }
                    return Optional.empty();
                });
    }

    private Mono<List<Long>> addNewRecords(String caseId, List<JournalpostDto> journalpostDtos) {
        return Flux.fromIterable(journalpostDtos)
                .concatMap(journalpostDto -> addNewRecord(caseId, journalpostDto))
                .collectList();
    }

    private Mono<Long> addNewRecord(String caseId, JournalpostDto journalpostDto) {
        return dispatchFiles(journalpostDto)
                .map(dokumentfilResourceLinkPerFileId ->
                        journalpostMappingService.toJournalpostResource(journalpostDto, dokumentfilResourceLinkPerFileId)
                )
                .doOnNext(journalpostResource -> log.debug("Creating new record: {}", journalpostResource))
                .map(JournalpostWrapper::new)
                .flatMap(journalpostWrapper -> fintArchiveClient.putRecord(caseId, journalpostWrapper))
                .doOnNext(journalpostNummer -> log.info("Added new record on case with id={} and journalpostNummer={}", caseId, journalpostNummer));
    }

    private Mono<Map<UUID, Link>> dispatchFiles(JournalpostDto journalpostDto) {
        return journalpostDto.getDokumentbeskrivelse()
                .map(this::dispatchFiles)
                .orElse(Mono.just(Map.of()));
    }

    private Mono<Map<UUID, Link>> dispatchFiles(Collection<DokumentbeskrivelseDto> dokumentbeskrivelseDtos) {
        return fileClient.dispatchFiles(
                dokumentbeskrivelseDtos
                        .stream()
                        .map(DokumentbeskrivelseDto::getDokumentobjekt)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(Collection::stream)
                        .map(DokumentobjektDto::getFileId)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList()
        );
    }

}
