package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JournalpostDto {
    private String tittel;
    private String offentligTittel;
    private LinkDto journalstatus;
    private LinkDto saksbehandler;
    private LinkDto journalposttype;
    private LinkDto administrativenhet;
    private SkjermingDto skjerming;
    private Collection<KorrespondansepartDto> korrespondansepart;
    private Collection<DokumentbeskrivelseDto> dokumentbeskrivelse;
}
