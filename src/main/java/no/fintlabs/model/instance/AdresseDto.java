package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdresseDto {
    private String adresselinje;
    private String postnummer;
    private String poststed;

    public Optional<String> getAdresselinje() {
        return Optional.ofNullable(adresselinje);
    }

    public Optional<String> getPostnummer() {
        return Optional.ofNullable(postnummer);
    }

    public Optional<String> getPoststed() {
        return Optional.ofNullable(poststed);
    }

}
