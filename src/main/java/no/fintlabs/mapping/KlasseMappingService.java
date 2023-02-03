package no.fintlabs.mapping;

import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.fintlabs.model.instance.KlasseDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KlasseMappingService {

    private final SkjermingMappingService skjermingMappingService;

    public KlasseMappingService(SkjermingMappingService skjermingMappingService) {
        this.skjermingMappingService = skjermingMappingService;
    }

    public List<KlasseResource> toKlasse(List<KlasseDto> klasseDtos) {
        if (klasseDtos == null) {
            return null;
        }
        return klasseDtos.stream().map(this::toKlasse).toList();
    }

    private KlasseResource toKlasse(KlasseDto klasseDto) {
        KlasseResource klasseResource = new KlasseResource();
        klasseResource.setKlasseId(klasseDto.getKlasseId());
        Optional.ofNullable(klasseDto.getSkjerming())
                .map(skjermingMappingService::toSkjermingResource)
                .ifPresent(klasseResource::setSkjerming);
        klasseResource.setTittel(klasseDto.getTittel());
        klasseResource.addKlassifikasjonssystem(klasseDto.getKlassifikasjonssystem());
        return klasseResource;
    }

}
