package no.fintlabs.mapping;

import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.fintlabs.model.instance.SkjermingDto;
import org.springframework.stereotype.Service;

@Service
public class SkjermingMappingService {

    public SkjermingResource toSkjermingResource(SkjermingDto skjermingDto) {
        if (skjermingDto == null) {
            return null;
        }
        SkjermingResource skjermingResource = new SkjermingResource();
        skjermingDto.getTilgangsrestriksjon().ifPresent(skjermingResource::addTilgangsrestriksjon);
        skjermingDto.getSkjermingshjemmel().ifPresent(skjermingResource::addSkjermingshjemmel);
        return skjermingResource;
    }

}
