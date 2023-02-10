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
    private LinkDto tilgangsrestriksjon;
    private LinkDto skjermingshjemmel;

    public Optional<LinkDto> getTilgangsrestriksjon() {
        return Optional.ofNullable(tilgangsrestriksjon);
    }

    public Optional<LinkDto> getSkjermingshjemmel() {
        return Optional.ofNullable(skjermingshjemmel);
    }

}
