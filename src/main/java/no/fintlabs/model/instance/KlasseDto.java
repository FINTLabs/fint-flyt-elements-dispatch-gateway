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
    private Integer rekkefolge;
    private String klassifikasjonssystem;
    private String klasseId;
    private String tittel;
    private SkjermingDto skjerming;

    public Optional<Integer> getRekkefolge() {
        return Optional.ofNullable(rekkefolge);
    }

    public Optional<String> getKlassifikasjonssystem() {
        return Optional.ofNullable(klassifikasjonssystem);
    }

    public Optional<String> getKlasseId() {
        return Optional.ofNullable(klasseId);
    }

    public Optional<SkjermingDto> getSkjerming() {
        return Optional.ofNullable(skjerming);
    }

    public Optional<String> getTittel() {
        return Optional.ofNullable(tittel);
    }

}
