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
public class KorrespondansepartDto {
    private String fodselsnummer;
    private String organisasjonsnummer;
    private String korrespondansepartNavn;
    private AdresseDto adresse;
    private Link korrespondanseparttype;
    private String kontaktperson;
    private KontaktinformasjonDto kontaktinformasjon;
    private SkjermingDto skjerming;
}
