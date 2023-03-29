package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JournalpostDto {
    private String tittel;
    private String offentligTittel;
    private String journalposttype;
    private String administrativenhet;
    private String saksbehandler;
    private String journalstatus;
    private SkjermingDto skjerming;
    private Collection<KorrespondansepartDto> korrespondansepart;
    private Collection<DokumentbeskrivelseDto> dokumentbeskrivelse;

    public Optional<String> getTittel() {
        return Optional.ofNullable(tittel);
    }

    public Optional<String> getOffentligTittel() {
        return Optional.ofNullable(offentligTittel);
    }

    public Optional<String> getJournalstatus() {
        return Optional.ofNullable(journalstatus);
    }

    public Optional<String> getSaksbehandler() {
        return Optional.ofNullable(saksbehandler);
    }

    public Optional<String> getJournalposttype() {
        return Optional.ofNullable(journalposttype);
    }

    public Optional<String> getAdministrativenhet() {
        return Optional.ofNullable(administrativenhet);
    }

    public Optional<SkjermingDto> getSkjerming() {
        return Optional.ofNullable(skjerming);
    }

    public Optional<Collection<KorrespondansepartDto>> getKorrespondansepart() {
        return Optional.ofNullable(korrespondansepart);
    }

    public Optional<Collection<DokumentbeskrivelseDto>> getDokumentbeskrivelse() {
        return Optional.ofNullable(dokumentbeskrivelse);
    }

}
