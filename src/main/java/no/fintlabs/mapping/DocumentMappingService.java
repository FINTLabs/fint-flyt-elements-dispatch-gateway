package no.fintlabs.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fint.model.resource.arkiv.noark.DokumentobjektResource;
import no.fintlabs.model.mappedinstance.Document;
import no.fintlabs.model.mappedinstance.MappedInstanceElement;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class DocumentMappingService {

    public List<DokumentbeskrivelseResource> toDokumentbeskrivelseResources(
            Collection<Document> documents,
            MappedInstanceElement documentInstanceElement
    ) {
        return documents
                .stream()
                .map(document -> toDokumentBeskrivelseResource(document, documentInstanceElement))
                .toList();
    }

    private DokumentbeskrivelseResource toDokumentBeskrivelseResource(
            Document document,
            MappedInstanceElement documentInstanceElement
    ) {
        DokumentbeskrivelseResource dokumentbeskrivelseResource = new DokumentbeskrivelseResource();
        dokumentbeskrivelseResource.setTittel(
                documentInstanceElement.getFieldValue("tittel").map(value -> value + "_").orElse("")
                        + document.getName()
        );
        documentInstanceElement.getFieldValue("dokumentStatus").map(Link::with).ifPresent(dokumentbeskrivelseResource::addDokumentstatus);

        DokumentobjektResource dokumentobjektResource = new DokumentobjektResource();
        documentInstanceElement.getFieldValue("DokumentBeskrivelse.dokumentObjekt.variantFormat")
                .map(Link::with)
                .ifPresent(dokumentobjektResource::addVariantFormat);

        // TODO: 25/10/2022 Add file
        dokumentbeskrivelseResource.setDokumentobjekt(List.of(dokumentobjektResource));
        return dokumentbeskrivelseResource;
    }

}
