package no.fintlabs.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fintlabs.model.instance.JournalpostDto;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class JournalpostMappingService {

    private final SkjermingMappingService skjermingMappingService;
    private final KorrespondansepartMappingService korrespondanseparMappingService;
    private final DokumentbeskrivelseMappingService dokumentbeskrivelseMappingService;

    public JournalpostMappingService(
            SkjermingMappingService skjermingMappingService,
            KorrespondansepartMappingService korrespondanseparMappingService,
            DokumentbeskrivelseMappingService dokumentbeskrivelseMappingService
    ) {
        this.skjermingMappingService = skjermingMappingService;
        this.korrespondanseparMappingService = korrespondanseparMappingService;
        this.dokumentbeskrivelseMappingService = dokumentbeskrivelseMappingService;
    }


    public JournalpostResource toJournalpostResource(
            JournalpostDto journalpostDto,
            Map<UUID, Link> fileArchiveLinkPerFileId
    ) {
        JournalpostResource journalpostResource = new JournalpostResource();
        journalpostResource.setTittel(journalpostDto.getTittel());
        journalpostResource.setOffentligTittel(journalpostDto.getOffentligTittel());
        journalpostResource.addSaksbehandler(journalpostDto.getSaksbehandler());
        journalpostResource.addJournalposttype(journalpostDto.getJournalposttype());
        journalpostResource.addAdministrativEnhet(journalpostDto.getAdministrativenhet());
        journalpostResource.setSkjerming(skjermingMappingService.toSkjermingResource(journalpostDto.getSkjerming()));
        journalpostResource.setKorrespondansepart(
                korrespondanseparMappingService.toKorrespondansepartResource(journalpostDto.getKorrespondansepart())
        );
        journalpostResource.setDokumentbeskrivelse(
                dokumentbeskrivelseMappingService.toDokumentbeskrivelseResource(
                        journalpostDto.getDokumentbeskrivelse(),
                        fileArchiveLinkPerFileId
                )
        );
        return journalpostResource;
    }

}
