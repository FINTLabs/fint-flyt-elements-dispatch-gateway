package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KlasseDto {
    private String klasseId;
    private SkjermingDto skjerming;
    private String tittel;
    private LinkDto klassifikasjonssystem;
    private Integer rekkefølge;
}
