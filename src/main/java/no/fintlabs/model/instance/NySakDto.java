package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NySakDto {
    private String tittel;
    private String offentligTittel;
    private LinkDto saksmappetype;
    private LinkDto saksstatus;
    private LinkDto journalenhet;
    private LinkDto administrativenhet;
    private LinkDto saksansvarlig;
    private LinkDto arkivdel;
    private SkjermingDto skjerming;
    private List<KlasseDto> klasse;
}
