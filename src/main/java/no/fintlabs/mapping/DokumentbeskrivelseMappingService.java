package no.fintlabs.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fintlabs.model.instance.DokumentbeskrivelseDto;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DokumentbeskrivelseMappingService {

    private final DokumentObjektMappingService dokumentObjektMappingService;

    public DokumentbeskrivelseMappingService(DokumentObjektMappingService dokumentObjektMappingService) {
        this.dokumentObjektMappingService = dokumentObjektMappingService;
    }

    public List<DokumentbeskrivelseResource> toDokumentbeskrivelseResource(
            Collection<DokumentbeskrivelseDto> dokumentbeskrivelseDto,
            Map<UUID, Link> fileArchiveLinkPerFileId
    ) {
        return dokumentbeskrivelseDto.stream()
                .map(dbDto -> toDokumentbeskrivelseResource(dbDto, fileArchiveLinkPerFileId))
                .toList();
    }

    public DokumentbeskrivelseResource toDokumentbeskrivelseResource(
            DokumentbeskrivelseDto dokumentbeskrivelseDto,
            Map<UUID, Link> fileArchiveLinkPerFileId
    ) {
        DokumentbeskrivelseResource dokumentbeskrivelseResource = new DokumentbeskrivelseResource();
        dokumentbeskrivelseDto.getTittel().ifPresent(dokumentbeskrivelseResource::setTittel);
        dokumentbeskrivelseDto.getDokumentType().ifPresent(dokumentbeskrivelseResource::addDokumentType);
        dokumentbeskrivelseDto.getTilknyttetRegistreringSom().ifPresent(dokumentbeskrivelseResource::addTilknyttetRegistreringSom);
        dokumentbeskrivelseDto.getDokumentstatus().ifPresent(dokumentbeskrivelseResource::addDokumentstatus);
        dokumentbeskrivelseDto.getDokumentobjekt()
                .map(dokumentobjektDtos -> dokumentObjektMappingService.toDokumentobjektResource(
                        dokumentobjektDtos,
                        fileArchiveLinkPerFileId
                ))
                .ifPresent(dokumentbeskrivelseResource::setDokumentobjekt);
        return dokumentbeskrivelseResource;
    }

}
