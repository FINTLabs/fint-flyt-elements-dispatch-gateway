package no.fintlabs;

import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.KorrespondansepartResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.mapping.MappingService;
import no.fintlabs.model.CreationStrategy;
import no.fintlabs.model.Result;
import no.fintlabs.model.mappedinstance.MappedInstance;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class DispatchService {

    private final MappingService mappingService;
    private final DispatchClient dispatchClient;

    public DispatchService(MappingService mappingService, DispatchClient dispatchClient) {
        this.mappingService = mappingService;
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
            case NEW -> dispatchNewCase(mappedInstance);
            case COLLECTION -> dispatchToCollectionCase(mappedInstance);
            case EXISTING -> dispatchToExistingOrNewCase(mappedInstance);
        };
    }

    private Mono<Result> dispatchNewCase(MappedInstance mappedInstance) {
        SakResource sakResource = mappingService.toSakResource(
                mappedInstance.getElement("case").orElseThrow()
        );
        JournalpostResource journalpostResource = createJournalpostResource(mappedInstance);
        sakResource.setJournalpost(List.of(journalpostResource));

        return dispatchClient.dispatchNewCase(sakResource);
    }

    private Mono<Result> dispatchToCollectionCase(MappedInstance mappedInstance) {
        String collectionCaseId = mappedInstance
                .getElement("case")
                .flatMap(mappedInstanceElement -> mappedInstanceElement.getFieldValue("saksnummer"))
                .orElseThrow();

        JournalpostResource journalpostResource = createJournalpostResource(mappedInstance);

        return dispatchClient.dispatchToCollectionCase(collectionCaseId, journalpostResource);
    }

    private Mono<Result> dispatchToExistingOrNewCase(MappedInstance mappedInstance) {
        SakResource sakResource = mappingService.toSakResource(mappedInstance.getElement("case").orElseThrow());
        JournalpostResource journalpostResource = createJournalpostResource(mappedInstance);
        sakResource.setJournalpost(List.of(journalpostResource));

        return dispatchClient.dispatchToExistingOrAsNewCase(sakResource);
    }

    private JournalpostResource createJournalpostResource(MappedInstance mappedInstance) {
        JournalpostResource journalpostResource = mappingService.toJournalpostResource(mappedInstance.getElement("record").orElseThrow());

        List<DokumentbeskrivelseResource> dokumentbeskrivelseResources = mappingService.toDokumentbeskrivelseResources(
                mappedInstance.getDocuments(),
                mappedInstance.getElement("document").orElseThrow()
        );

        KorrespondansepartResource korrespondansepartResource = mappingService.toKorrespondansepartResource(
                mappedInstance.getElement("applicant").orElseThrow()
        );

        journalpostResource.setKorrespondansepart(List.of(korrespondansepartResource));
        journalpostResource.setDokumentbeskrivelse(dokumentbeskrivelseResources);

        return journalpostResource;
    }
}
