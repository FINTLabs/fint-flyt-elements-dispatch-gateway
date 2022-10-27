package no.fintlabs;

import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.KorrespondansepartResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.mapping.ApplicantMappingService;
import no.fintlabs.mapping.CaseMappingService;
import no.fintlabs.mapping.DocumentMappingService;
import no.fintlabs.mapping.RecordMappingService;
import no.fintlabs.model.CreationStrategy;
import no.fintlabs.model.Result;
import no.fintlabs.model.mappedinstance.MappedInstance;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class DispatchService {

    private final CaseMappingService caseMappingService;
    private final RecordMappingService recordMappingService;
    private final ApplicantMappingService applicantMappingService;
    private final DocumentMappingService documentMappingService;
    private final DispatchClient dispatchClient;

    public DispatchService(
            CaseMappingService caseMappingService,
            RecordMappingService recordMappingService,
            ApplicantMappingService applicantMappingService,
            DocumentMappingService documentMappingService,
            DispatchClient dispatchClient
    ) {
        this.caseMappingService = caseMappingService;
        this.recordMappingService = recordMappingService;
        this.applicantMappingService = applicantMappingService;
        this.documentMappingService = documentMappingService;
        this.dispatchClient = dispatchClient;
    }

    public Mono<Result> dispatch(MappedInstance mappedInstance) {
        CreationStrategy creationStrategy = CreationStrategy.valueOf(
                mappedInstance
                        .getElement("case")
                        .flatMap(mappedInstanceElement -> mappedInstanceElement.getFieldValue("creationStrategy"))
                        .orElseThrow()
        );
        return switch (creationStrategy) {
            case NEW -> processAsNewCase(mappedInstance);
            case COLLECTION -> processAsNewRecordForCollectionCase(mappedInstance);
            case EXISTING -> processAsNewRecordForExistingCaseOrNewCase(mappedInstance);
        };
    }

    private Mono<Result> processAsNewCase(MappedInstance mappedInstance) {
        SakResource sakResource = caseMappingService.toSakResource(
                mappedInstance.getElement("case").orElseThrow()
        );
        JournalpostResource journalpostResource = createJournalpostResource(mappedInstance);
        sakResource.setJournalpost(List.of(journalpostResource));

        return dispatchClient.dispatchNewCase(sakResource);
    }

    private Mono<Result> processAsNewRecordForCollectionCase(MappedInstance mappedInstance) {
        String collectionCaseId = mappedInstance
                .getElement("case")
                .flatMap(mappedInstanceElement -> mappedInstanceElement.getFieldValue("saksnummer"))
                .orElseThrow();

        JournalpostResource journalpostResource = createJournalpostResource(mappedInstance);

        return dispatchClient.dispatchToCollectionCase(collectionCaseId, journalpostResource);
    }

    private Mono<Result> processAsNewRecordForExistingCaseOrNewCase(MappedInstance mappedInstance) {
        SakResource sakResource = caseMappingService.toSakResource(mappedInstance.getElement("case").orElseThrow());
        JournalpostResource journalpostResource = createJournalpostResource(mappedInstance);
        sakResource.setJournalpost(List.of(journalpostResource));

        return dispatchClient.dispatchToExistingOrAsNewCase(sakResource);
    }

    private JournalpostResource createJournalpostResource(MappedInstance mappedInstance) {
        JournalpostResource journalpostResource = recordMappingService.toJournalpostResource(mappedInstance.getElement("record").orElseThrow());

        List<DokumentbeskrivelseResource> dokumentbeskrivelseResources = documentMappingService.toDokumentbeskrivelseResources(
                mappedInstance.getDocuments(),
                mappedInstance.getElement("document").orElseThrow()
        );

        KorrespondansepartResource korrespondansepartResource = applicantMappingService.toKorrespondansepartResource(
                mappedInstance.getElement("applicant").orElseThrow()
        );

        journalpostResource.setKorrespondansepart(List.of(korrespondansepartResource));
        journalpostResource.setDokumentbeskrivelse(dokumentbeskrivelseResources);

        return journalpostResource;
    }
}
