package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KorrespondansepartDto {
    private String fodselsnummer;
    private String organisasjonsnummer;
    private String korrespondansepartNavn;
    private AdresseDto adresse;
    private LinkDto korrespondanseparttype;
    private String kontaktperson;
    private KontaktinformasjonDto kontaktinformasjon;
    private SkjermingDto skjerming;

    public Optional<String> getFodselsnummer() {
        return Optional.ofNullable(fodselsnummer);
    }

    public Optional<String> getOrganisasjonsnummer() {
        return Optional.ofNullable(organisasjonsnummer);
    }

    public Optional<String> getKorrespondansepartNavn() {
        return Optional.ofNullable(korrespondansepartNavn);
    }

    public Optional<AdresseDto> getAdresse() {
        return Optional.ofNullable(adresse);
    }

    public Optional<LinkDto> getKorrespondanseparttype() {
        return Optional.ofNullable(korrespondanseparttype);
    }

    public Optional<String> getKontaktperson() {
        return Optional.ofNullable(kontaktperson);
    }

    public Optional<KontaktinformasjonDto> getKontaktinformasjon() {
        return Optional.ofNullable(kontaktinformasjon);
    }

    public Optional<SkjermingDto> getSkjerming() {
        return Optional.ofNullable(skjerming);
    }

}
