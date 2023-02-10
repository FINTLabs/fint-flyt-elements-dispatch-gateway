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
public class KlasseDto {
    private String klasseId;
    private SkjermingDto skjerming;
    private String tittel;
    private String klassifikasjonssystem;
    private Integer rekkefølge;

    public Optional<String> getKlasseId() {
        return Optional.ofNullable(klasseId);
    }

    public Optional<SkjermingDto> getSkjerming() {
        return Optional.ofNullable(skjerming);
    }

    public Optional<String> getTittel() {
        return Optional.ofNullable(tittel);
    }

    public Optional<String> getKlassifikasjonssystem() {
        return Optional.ofNullable(klassifikasjonssystem);
    }

    public Optional<Integer> getRekkefølge() {
        return Optional.ofNullable(rekkefølge);
    }

}
