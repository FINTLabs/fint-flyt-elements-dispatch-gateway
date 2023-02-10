package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SkjermingDto {
    private String tilgangsrestriksjon;
    private String skjermingshjemmel;

    public Optional<String> getTilgangsrestriksjon() {
        return Optional.ofNullable(tilgangsrestriksjon);
    }

    public Optional<String> getSkjermingshjemmel() {
        return Optional.ofNullable(skjermingshjemmel);
    }

}
