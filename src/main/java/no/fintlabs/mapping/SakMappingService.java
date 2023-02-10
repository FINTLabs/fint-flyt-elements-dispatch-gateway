package no.fintlabs.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.model.instance.NySakDto;
import org.springframework.stereotype.Service;

@Service
public class SakMappingService {

    private final SkjermingMappingService skjermingMappingService;
    private final KlasseMappingService klasseMappingService;

    public SakMappingService(
            SkjermingMappingService skjermingMappingService,
            KlasseMappingService klasseMappingService
    ) {
        this.skjermingMappingService = skjermingMappingService;
        this.klasseMappingService = klasseMappingService;
    }

    public SakResource toSakResource(NySakDto nySakDto) {
        if (nySakDto == null) {
            return null;
        }
        SakResource sakResource = new SakResource();
        nySakDto.getTittel().ifPresent(sakResource::setTittel);
        nySakDto.getOffentligTittel().ifPresent(sakResource::setOffentligTittel);
        nySakDto.getSaksmappetype().map(Link::with).ifPresent(sakResource::addSaksmappetype);
        nySakDto.getSaksstatus().map(Link::with).ifPresent(sakResource::addSaksstatus);
        nySakDto.getJournalenhet().map(Link::with).ifPresent(sakResource::addJournalenhet);
        nySakDto.getAdministrativenhet().map(Link::with).ifPresent(sakResource::addAdministrativEnhet);
        nySakDto.getSaksansvarlig().map(Link::with).ifPresent(sakResource::addSaksansvarlig);
        nySakDto.getArkivdel().map(Link::with).ifPresent(sakResource::addArkivdel);
        nySakDto.getKlasse()
                .map(klasseMappingService::toKlasse)
                .ifPresent(sakResource::setKlasse);
        nySakDto.getSkjerming()
                .map(skjermingMappingService::toSkjermingResource)
                .ifPresent(sakResource::setSkjerming);
        return sakResource;
    }

}
