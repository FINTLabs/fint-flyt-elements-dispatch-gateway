package no.fintlabs.mapping;

import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.fintlabs.model.instance.AdresseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdresseMappingService {

    public AdresseResource toAdresseResource(AdresseDto adresseDto) {
        AdresseResource adresseResource = new AdresseResource();
        adresseDto.getAdresselinje().map(List::of).ifPresent(adresseResource::setAdresselinje);
        adresseDto.getPostnummer().ifPresent(adresseResource::setPostnummer);
        adresseDto.getPoststed().ifPresent(adresseResource::setPoststed);
        return adresseResource;
    }

}
