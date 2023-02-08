package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KorrespondansepartDto {
    private String fodselsnummer;
    private String organisasjonsnummer;
    private String korrespondansepartNavn;
    private AdresseDto adresse;
    private LinkDto korrespondanseparttype;
    private String kontaktperson;
    private KontaktinformasjonDto kontaktinformasjon;
    private SkjermingDto skjerming;
}
