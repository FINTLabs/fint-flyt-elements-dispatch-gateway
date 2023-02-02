package no.fintlabs.mapping;

import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.fintlabs.model.instance.AdresseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdresseMappingService {

    public AdresseResource toAdresseResource(AdresseDto adresseDto) {
        AdresseResource adresseResource = new AdresseResource();
        adresseResource.setAdresselinje(List.of(adresseDto.getAdresselinje()));
        adresseResource.setPostnummer(adresseDto.getPostnummer());
        adresseResource.setPoststed(adresseDto.getPoststed());
        return adresseResource;
    }

}
