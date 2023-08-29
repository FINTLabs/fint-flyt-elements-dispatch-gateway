package no.fintlabs.model.instance;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import javax.validation.Valid;
import java.util.Optional;

@Builder
@Jacksonized
public class PartDto {
    private final String partNavn;
    private final String partRolle;
    private final String kontaktperson;
    private final @Valid AdresseDto adresse;
    private final @Valid KontaktinformasjonDto kontaktinformasjon;

    public Optional<String> getPartNavn() {
        return Optional.ofNullable(partNavn);
    }

    public Optional<String> getPartRolle() {
        return Optional.ofNullable(partRolle);
    }

    public Optional<String> getKontaktperson() {
        return Optional.ofNullable(kontaktperson);
    }

    public Optional<AdresseDto> getAdresse() {
        return Optional.ofNullable(adresse);
    }

    public Optional<KontaktinformasjonDto> getKontaktinformasjon() {
        return Optional.ofNullable(kontaktinformasjon);
    }

    @Override
    public String toString() {
        return "Sensitive data omitted";
    }
}
