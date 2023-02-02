package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fint.model.resource.Link;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KlasseDto {
    private String klasseId;
    private SkjermingDto skjerming;
    private String tittel;
    private Link klassifikasjonssystem;
}
