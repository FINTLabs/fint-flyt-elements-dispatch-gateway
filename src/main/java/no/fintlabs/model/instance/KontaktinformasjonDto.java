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
public class KontaktinformasjonDto {
    private String epostadresse;
    private String mobiltelefonnummer;
    private String telefonnummer;

    public Optional<String> getEpostadresse() {
        return Optional.ofNullable(epostadresse);
    }

    public Optional<String> getTelefonnummer() {
        return Optional.ofNullable(telefonnummer);
    }

    public Optional<String> getMobiltelefonnummer() {
        return Optional.ofNullable(mobiltelefonnummer);
    }

}
