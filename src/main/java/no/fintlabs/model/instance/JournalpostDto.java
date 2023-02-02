package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fint.model.resource.Link;

import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JournalpostDto {
    private String tittel;
    private String offentligTittel;
    private Link journalstatus;
    private Link saksbehandler;
    private Link journalposttype;
    private Link administrativenhet;
    private SkjermingDto skjerming;
    private Collection<KorrespondansepartDto> korrespondansepart;
    private Collection<DokumentbeskrivelseDto> dokumentbeskrivelse;
}
