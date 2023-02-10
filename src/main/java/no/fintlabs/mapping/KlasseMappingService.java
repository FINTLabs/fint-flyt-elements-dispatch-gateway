package no.fintlabs.mapping;

import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.fintlabs.model.instance.KlasseDto;
import org.springframework.stereotype.Service;

import java.util.List;

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
        klasseDto.getKlasseId().ifPresent(klasseResource::setKlasseId);
        klasseDto.getSkjerming()
                .map(skjermingMappingService::toSkjermingResource)
                .ifPresent(klasseResource::setSkjerming);
        klasseDto.getTittel().ifPresent(klasseResource::setTittel);
        klasseDto.getKlassifikasjonssystem().ifPresent(klasseResource::addKlassifikasjonssystem);
        klasseDto.getRekkefølge().ifPresent(klasseResource::setRekkefolge);
        return klasseResource;
    }

}
