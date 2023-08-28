package no.fintlabs.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fint.model.resource.arkiv.noark.JournalpostResource;

import java.util.Collection;
import java.util.List;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class JournalpostWrapper {

    public JournalpostWrapper(JournalpostResource journalpost) {
        this.journalpost = List.of(journalpost);
    }

    private Collection<JournalpostResource> journalpost;

    @Override
    public String toString() {
        return "Sensitive data omitted";
    }

}
