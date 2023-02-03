package no.fintlabs.mapping;

import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fintlabs.model.instance.KontaktinformasjonDto;
import org.springframework.stereotype.Service;

@Service
public class KontaktinformasjonMappingService {

    public Kontaktinformasjon toKontaktinformasjon(KontaktinformasjonDto kontaktinformasjonDto) {
        Kontaktinformasjon kontaktinformasjon = new Kontaktinformasjon();
        kontaktinformasjon.setEpostadresse(kontaktinformasjonDto.getEpostadresse());
        kontaktinformasjon.setTelefonnummer(kontaktinformasjonDto.getTelefonnummer());
        kontaktinformasjon.setMobiltelefonnummer(kontaktinformasjonDto.getMobiltelefonnummer());
        return kontaktinformasjon;
    }

}
