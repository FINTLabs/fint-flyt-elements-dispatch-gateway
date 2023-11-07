package no.fintlabs.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fintlabs.model.instance.JournalpostDto;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
            JournalpostDto journalpostDto
    ) {
        return toJournalpostResource(journalpostDto, Collections.emptyMap());
    }

    public JournalpostResource toJournalpostResource(
            JournalpostDto journalpostDto,
            Map<UUID, Link> fileArchiveLinkPerFileId
    ) {
        JournalpostResource journalpostResource = new JournalpostResource();

        journalpostDto.getTittel().ifPresent(journalpostResource::setTittel);
        journalpostDto.getOffentligTittel().ifPresent(journalpostResource::setOffentligTittel);
        journalpostDto.getJournalstatus().map(Link::with).ifPresent(journalpostResource::addJournalstatus);
        journalpostDto.getSaksbehandler().map(Link::with).ifPresent(journalpostResource::addSaksbehandler);
        journalpostDto.getJournalposttype().map(Link::with).ifPresent(journalpostResource::addJournalposttype);
        journalpostDto.getAdministrativEnhet().map(Link::with).ifPresent(journalpostResource::addAdministrativEnhet);

        journalpostDto.getSkjerming()
                .map(skjermingMappingService::toSkjermingResource)
                .ifPresent(journalpostResource::setSkjerming);

        journalpostDto.getKorrespondansepart()
                .map(korrespondanseparMappingService::toKorrespondansepartResource)
                .ifPresent(journalpostResource::setKorrespondansepart);

        journalpostDto.getDokumentbeskrivelse()
                .map(dokumentBeskrivelseDtos -> dokumentbeskrivelseMappingService.toDokumentbeskrivelseResource(
                        dokumentBeskrivelseDtos, fileArchiveLinkPerFileId
                ))
                .ifPresent(journalpostResource::setDokumentbeskrivelse);

        return journalpostResource;
    }

}
