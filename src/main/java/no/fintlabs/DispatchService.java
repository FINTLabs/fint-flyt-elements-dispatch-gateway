package no.fintlabs;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.KorrespondansepartResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.mapping.ApplicantMappingService;
import no.fintlabs.mapping.CaseMappingService;
import no.fintlabs.mapping.DocumentMappingService;
import no.fintlabs.mapping.RecordMappingService;
import no.fintlabs.model.CaseDispatchType;
import no.fintlabs.model.Result;
import no.fintlabs.model.mappedinstance.Document;
import no.fintlabs.model.mappedinstance.MappedInstance;
import no.fintlabs.web.archive.FintArchiveClient;
import no.fintlabs.web.file.FileClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class DispatchService {

    private final CaseMappingService caseMappingService;
    private final RecordMappingService recordMappingService;
    private final ApplicantMappingService applicantMappingService;
    private final DocumentMappingService documentMappingService;
    private final FileClient fileClient;
    private final FintArchiveClient fintArchiveClient;

    public DispatchService(
            CaseMappingService caseMappingService,
            RecordMappingService recordMappingService,
            ApplicantMappingService applicantMappingService,
            DocumentMappingService documentMappingService,
            FileClient fileClient,
            FintArchiveClient fintArchiveClient
    ) {
        this.caseMappingService = caseMappingService;
        this.recordMappingService = recordMappingService;
        this.applicantMappingService = applicantMappingService;
        this.documentMappingService = documentMappingService;
        this.fileClient = fileClient;
        this.fintArchiveClient = fintArchiveClient;
    }

    public Mono<Result> process(MappedInstance mappedInstance) {
        return getCase(mappedInstance)
                .flatMap(sakResource -> addRecord(sakResource, mappedInstance))
                .map(resultSakResource -> Result.accepted(resultSakResource.getMappeId().getIdentifikatorverdi()))
                .onErrorResume(WebClientResponseException.class, e ->
                        Mono.just(Result.declined(e.getResponseBodyAsString()))
                )
                .onErrorReturn(RuntimeException.class, Result.failed());
    }

    private Mono<SakResource> getCase(MappedInstance mappedInstance) {
        return switch (getCaseDispatchType(mappedInstance)) {
            case NEW -> createNewCase(mappedInstance);
            case BY_ID -> getCaseById(mappedInstance);
            case BY_SEARCH_OR_NEW -> getCaseBySearch(mappedInstance)
                    .flatMap(optionalCase -> optionalCase
                            .map(Mono::just)
                            .orElse(createNewCase(mappedInstance)));
        };
    }

    private CaseDispatchType getCaseDispatchType(MappedInstance mappedInstance) {
        return CaseDispatchType.valueOf(mappedInstance
                .getElement("case")
                .flatMap(mappedInstanceElement -> mappedInstanceElement.getFieldValue("type"))
                .orElseThrow()
        );
    }

    private Mono<SakResource> createNewCase(MappedInstance mappedInstance) {
        SakResource sakResource = caseMappingService.toSakResource(
                mappedInstance.getElement("case").orElseThrow()
        );
        return fintArchiveClient.postCase(sakResource);
    }

    private Mono<SakResource> getCaseById(MappedInstance mappedInstance) {
        String archiveCaseId = mappedInstance
                .getElement("case")
                .flatMap(mappedInstanceElement -> mappedInstanceElement.getFieldValue("id"))
                .orElseThrow();
        return fintArchiveClient.getCase(archiveCaseId);
    }

    private Mono<Optional<SakResource>> getCaseBySearch(MappedInstance mappedInstance) {
        throw new UnsupportedOperationException();
    }

    private Mono<SakResource> addRecord(SakResource sakResource, MappedInstance mappedInstance) {
        return addNewRecord(sakResource, mappedInstance);
    }

    private Mono<SakResource> addNewRecord(SakResource sakResource, MappedInstance mappedInstance) {
        return processFiles(mappedInstance)
                .map(dokumentfilResourceLinkPerFileId ->
                        createJournalpostResource(mappedInstance, dokumentfilResourceLinkPerFileId)
                )
                .map(journalpostResource -> {
                    sakResource.getJournalpost().add(journalpostResource);
                    return sakResource;
                })
                .flatMap(fintArchiveClient::putCase);
    }

    private Mono<Map<UUID, Link>> processFiles(MappedInstance mappedInstance) {
        return Flux.fromIterable(mappedInstance.getDocuments())
                .map(Document::getFileId)
                .flatMap(fileId -> Mono.zip(
                        Mono.just(fileId),
                        fileClient.getFile(fileId).flatMap(
                                file -> fintArchiveClient.postFile(file)
                                        .map(URI::toString)
                                        .map(Link::with)
                        )
                ))
                .collectMap(Tuple2::getT1, Tuple2::getT2);
    }

    private JournalpostResource createJournalpostResource(
            MappedInstance mappedInstance,
            Map<UUID, Link> dokumentfilResourceLinkPerFileId
    ) {
        JournalpostResource journalpostResource = recordMappingService.toJournalpostResource(mappedInstance.getElement("record").orElseThrow());

        List<DokumentbeskrivelseResource> dokumentbeskrivelseResources = documentMappingService.toDokumentbeskrivelseResources(
                mappedInstance.getDocuments(),
                mappedInstance.getElement("document").orElseThrow(),
                dokumentfilResourceLinkPerFileId
        );

        KorrespondansepartResource korrespondansepartResource = applicantMappingService.toKorrespondansepartResource(
                mappedInstance.getElement("applicant").orElseThrow()
        );

        journalpostResource.setKorrespondansepart(List.of(korrespondansepartResource));
        journalpostResource.setDokumentbeskrivelse(dokumentbeskrivelseResources);

        return journalpostResource;
    }

}
