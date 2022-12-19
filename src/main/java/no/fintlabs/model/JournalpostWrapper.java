package no.fintlabs.model;

import lombok.Data;
import no.fint.model.resource.arkiv.noark.JournalpostResource;

import java.util.Collection;
import java.util.List;

@Data
public class JournalpostWrapper {

    public JournalpostWrapper(JournalpostResource journalpost) {
        this.journalpost = List.of(journalpost);
    }

    private Collection<JournalpostResource> journalpost;
}
