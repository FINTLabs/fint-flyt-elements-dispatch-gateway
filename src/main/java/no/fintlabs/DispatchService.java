package no.fintlabs;

import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.KorrespondansepartResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.model.CreationStrategy;
import no.fintlabs.model.mappedinstance.MappedInstance;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DispatchService {

    private final MappingService mappingService;
    private final DispatchClient dispatchClient;

    public DispatchService(MappingService mappingService, DispatchClient dispatchClient) {
        this.mappingService = mappingService;
        this.dispatchClient = dispatchClient;
    }

    public Object dispatch(MappedInstance mappedInstance) {
        CreationStrategy creationStrategy = CreationStrategy.valueOf(
                mappedInstance
                        .getElement("case")
                        .getFieldValue("creationStrategy")
        );

        return switch (creationStrategy) {
            case NEW -> dispatchNewCase(mappedInstance);
            case COLLECTION -> dispatchToCollectionCase(mappedInstance);
            case EXISTING -> dispatchToExistingOrNewCase(mappedInstance);
        };
    }

    private Object dispatchNewCase(MappedInstance mappedInstance) {
        SakResource sakResource = mappingService.toSakResource(mappedInstance.getElement("case"));
        JournalpostResource journalpostResource = createJournalpostResource(mappedInstance);
        sakResource.setJournalpost(List.of(journalpostResource));

        return dispatchClient.dispatchNewCase(sakResource);
    }

    private Object dispatchToCollectionCase(MappedInstance mappedInstance) {
        String collectionCaseId = mappedInstance
                .getElement("case")
                .getFieldValue("saksnummer");

        JournalpostResource journalpostResource = createJournalpostResource(mappedInstance);

        return dispatchClient.dispatchToCollectionCase(collectionCaseId, journalpostResource);
    }

    private Object dispatchToExistingOrNewCase(MappedInstance mappedInstance) {
        return new Object();
    }

    private JournalpostResource createJournalpostResource(MappedInstance mappedInstance) {
        JournalpostResource journalpostResource = mappingService.toJournalpostResource(mappedInstance.getElement("record"));

        List<DokumentbeskrivelseResource> dokumentbeskrivelseResources = mappingService.toDokumentbeskrivelseResources(
                mappedInstance.getDocuments(),
                mappedInstance.getElement("document")
        );

        KorrespondansepartResource korrespondansepartResource = mappingService.toKorrespondansepartResource(
                mappedInstance.getElement("applicant")
        );

        journalpostResource.setKorrespondansepart(List.of(korrespondansepartResource));
        journalpostResource.setDokumentbeskrivelse(dokumentbeskrivelseResources);

        return journalpostResource;
    }
}
