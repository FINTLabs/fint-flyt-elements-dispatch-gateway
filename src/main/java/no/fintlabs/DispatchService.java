package no.fintlabs;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.KorrespondansepartResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.mapping.*;
import no.fintlabs.model.CreationStrategy;
import no.fintlabs.model.Result;
import no.fintlabs.model.mappedinstance.Document;
import no.fintlabs.model.mappedinstance.MappedInstance;
import no.fintlabs.web.archive.FintArchiveService;
import no.fintlabs.web.file.FileClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DispatchService {

    private final FileMappingService fileMappingService;
    private final CaseMappingService caseMappingService;
    private final RecordMappingService recordMappingService;
    private final ApplicantMappingService applicantMappingService;
    private final DocumentMappingService documentMappingService;
    private final FileClient fileClient;
    private final FintArchiveService fintArchiveService;

    public DispatchService(
            FileMappingService fileMappingService,
            CaseMappingService caseMappingService,
            RecordMappingService recordMappingService,
            ApplicantMappingService applicantMappingService,
            DocumentMappingService documentMappingService,
            FileClient fileClient,
            FintArchiveService fintArchiveService
    ) {
        this.fileMappingService = fileMappingService;
        this.caseMappingService = caseMappingService;
        this.recordMappingService = recordMappingService;
        this.applicantMappingService = applicantMappingService;
        this.documentMappingService = documentMappingService;
        this.fileClient = fileClient;
        this.fintArchiveService = fintArchiveService;
    }

    public Mono<Result> process(MappedInstance mappedInstance) {
        return processFiles(mappedInstance)
                .flatMap(dokumentfilResourceLinkPerFileId ->
                        switch (getCreationStrategy(mappedInstance)) {
                            case NEW -> processAsNewCase(mappedInstance, dokumentfilResourceLinkPerFileId);
                            case COLLECTION -> processAsNewRecordForCollectionCase(mappedInstance, dokumentfilResourceLinkPerFileId);
                            case EXISTING -> processAsNewRecordForExistingCaseOrNewCase(mappedInstance, dokumentfilResourceLinkPerFileId);
                        });
    }

    private CreationStrategy getCreationStrategy(MappedInstance mappedInstance) {
        return CreationStrategy.valueOf(mappedInstance
                .getElement("case")
                .flatMap(mappedInstanceElement -> mappedInstanceElement.getFieldValue("creationStrategy"))
                .orElseThrow()
        );
    }

    private Mono<Map<UUID, Link>> processFiles(MappedInstance mappedInstance) {
        return Flux.fromIterable(mappedInstance.getDocuments())
                .map(Document::getFileId)
                .flatMap(fileId -> Mono.zip(
                        Mono.just(fileId),
                        fileClient.getFile(fileId)
                                .map(fileMappingService::mapToDokumentfilResource)
                                .flatMap(fintArchiveService::dispatchFile)
                ))
                .collectMap(Tuple2::getT1, Tuple2::getT2);
    }

    private Mono<Result> processAsNewCase(
            MappedInstance mappedInstance,
            Map<UUID, Link> dokumentfilResourceLinkPerFileId
    ) {
        SakResource sakResource = caseMappingService.toSakResource(
                mappedInstance.getElement("case").orElseThrow()
        );
        JournalpostResource journalpostResource = createJournalpostResource(mappedInstance, dokumentfilResourceLinkPerFileId);
        sakResource.setJournalpost(List.of(journalpostResource));

        return fintArchiveService.dispatchNewCase(sakResource);
    }

    private Mono<Result> processAsNewRecordForCollectionCase(
            MappedInstance mappedInstance,
            Map<UUID, Link> dokumentfilResourceLinkPerFileId
    ) {
        String collectionCaseId = mappedInstance
                .getElement("case")
                .flatMap(mappedInstanceElement -> mappedInstanceElement.getFieldValue("saksnummer"))
                .orElseThrow();

        JournalpostResource journalpostResource = createJournalpostResource(mappedInstance, dokumentfilResourceLinkPerFileId);

        return fintArchiveService.dispatchToCollectionCase(collectionCaseId, journalpostResource);
    }

    private Mono<Result> processAsNewRecordForExistingCaseOrNewCase(
            MappedInstance mappedInstance,
            Map<UUID, Link> dokumentfilResourceLinkPerFileId
    ) {
        SakResource sakResource = caseMappingService.toSakResource(mappedInstance.getElement("case").orElseThrow());
        JournalpostResource journalpostResource = createJournalpostResource(mappedInstance, dokumentfilResourceLinkPerFileId);
        sakResource.setJournalpost(List.of(journalpostResource));

        return fintArchiveService.dispatchToExistingOrAsNewCase(sakResource);
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
