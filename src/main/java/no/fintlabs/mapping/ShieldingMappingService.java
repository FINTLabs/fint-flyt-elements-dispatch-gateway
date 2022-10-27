package no.fintlabs.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.fintlabs.model.mappedinstance.MappedInstanceElement;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShieldingMappingService {

    public Optional<SkjermingResource> toSkjermingResource(MappedInstanceElement mappedInstanceElement) {
        Optional<String> tilgangsrestriksjon = mappedInstanceElement.getFieldValue("tilgangsrestriksjon");
        Optional<String> skjermingshjemmel = mappedInstanceElement.getFieldValue("skjermingshjemmel");
        if (tilgangsrestriksjon.isEmpty() && skjermingshjemmel.isEmpty()) {
            return Optional.empty();
        }
        SkjermingResource skjermingResource = new SkjermingResource();
        tilgangsrestriksjon.map(Link::with).ifPresent(skjermingResource::addTilgangsrestriksjon);
        skjermingshjemmel.map(Link::with).ifPresent(skjermingResource::addSkjermingshjemmel);
        return Optional.of(skjermingResource);
    }

}
