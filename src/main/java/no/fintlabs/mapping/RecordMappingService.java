package no.fintlabs.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fintlabs.model.mappedinstance.MappedInstanceElement;
import org.springframework.stereotype.Service;

@Service
public class RecordMappingService {

    private final ShieldingMappingService shieldingMappingService;

    public RecordMappingService(ShieldingMappingService shieldingMappingService) {
        this.shieldingMappingService = shieldingMappingService;
    }

    public JournalpostResource toJournalpostResource(MappedInstanceElement recordInstanceElement) {
        JournalpostResource journalpostResource = new JournalpostResource();
        recordInstanceElement.getFieldValue("tittel").ifPresent(journalpostResource::setTittel);
        recordInstanceElement.getFieldValue("offentligTittel").ifPresent(journalpostResource::setOffentligTittel);
        recordInstanceElement.getFieldValue("administrativenhet").map(Link::with).ifPresent(journalpostResource::addAdministrativEnhet);
        recordInstanceElement.getFieldValue("journalstatus").map(Link::with).ifPresent(journalpostResource::addJournalstatus);
        recordInstanceElement.getFieldValue("journalposttype").map(Link::with).ifPresent(journalpostResource::addJournalposttype);
        recordInstanceElement.getFieldValue("saksbehandler").map(Link::with).ifPresent(journalpostResource::addSaksbehandler);

        shieldingMappingService.toSkjermingResource(recordInstanceElement).ifPresent(journalpostResource::setSkjerming);

        return journalpostResource;
    }

}
