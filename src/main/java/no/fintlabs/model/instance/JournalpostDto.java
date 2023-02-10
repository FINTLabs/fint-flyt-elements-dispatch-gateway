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
    private LinkDto journalstatus;
    private LinkDto saksbehandler;
    private LinkDto journalposttype;
    private LinkDto administrativenhet;
    private SkjermingDto skjerming;
    private Collection<KorrespondansepartDto> korrespondansepart;
    private Collection<DokumentbeskrivelseDto> dokumentbeskrivelse;

    public Optional<String> getTittel() {
        return Optional.ofNullable(tittel);
    }

    public Optional<String> getOffentligTittel() {
        return Optional.ofNullable(offentligTittel);
    }

    public Optional<LinkDto> getJournalstatus() {
        return Optional.ofNullable(journalstatus);
    }

    public Optional<LinkDto> getSaksbehandler() {
        return Optional.ofNullable(saksbehandler);
    }

    public Optional<LinkDto> getJournalposttype() {
        return Optional.ofNullable(journalposttype);
    }

    public Optional<LinkDto> getAdministrativenhet() {
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
