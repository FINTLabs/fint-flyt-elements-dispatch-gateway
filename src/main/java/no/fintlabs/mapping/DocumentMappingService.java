package no.fintlabs.mapping;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fint.model.resource.arkiv.noark.DokumentobjektResource;
import no.fintlabs.model.mappedinstance.Document;
import no.fintlabs.model.mappedinstance.MappedInstanceElement;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class DocumentMappingService {

    public List<DokumentbeskrivelseResource> toDokumentbeskrivelseResources(
            Collection<Document> documents,
            MappedInstanceElement documentInstanceElement,
            Map<UUID, Link> dokumentfilResourceLinkPerFileId
    ) {
        return documents
                .stream()
                .map(document -> toDokumentBeskrivelseResource(document, documentInstanceElement, dokumentfilResourceLinkPerFileId))
                .toList();
    }

    private DokumentbeskrivelseResource toDokumentBeskrivelseResource(
            Document document,
            MappedInstanceElement documentInstanceElement,
            Map<UUID, Link> dokumentfilResourceLinkPerFileId
    ) {
        DokumentbeskrivelseResource dokumentbeskrivelseResource = new DokumentbeskrivelseResource();
        dokumentbeskrivelseResource.setTittel(
                documentInstanceElement.getFieldValue("tittel").map(value -> value + "_").orElse("")
                        + document.getName()
        );
        documentInstanceElement.getFieldValue("dokumentStatus").map(Link::with).ifPresent(dokumentbeskrivelseResource::addDokumentstatus);
        documentInstanceElement.getFieldValue("dokumentType").map(Link::with).ifPresent(dokumentbeskrivelseResource::addDokumentType);

        DokumentobjektResource dokumentobjektResource = new DokumentobjektResource();
        documentInstanceElement.getFieldValue("dokumentObjekt.variantFormat").map(Link::with).ifPresent(dokumentobjektResource::addVariantFormat);
        dokumentobjektResource.addFilformat(Link.with("https://beta.felleskomponent.no/arkiv/kodeverk/format/systemid/PROD"));
        dokumentobjektResource.addReferanseDokumentfil(
                dokumentfilResourceLinkPerFileId.get(document.getFileId())
        );

        dokumentbeskrivelseResource.setDokumentobjekt(List.of(dokumentobjektResource));
        return dokumentbeskrivelseResource;
    }

}
