package no.fintlabs.mapping;

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
        nySakDto.getSaksmappetype().ifPresent(sakResource::addSaksmappetype);
        nySakDto.getSaksstatus().ifPresent(sakResource::addSaksstatus);
        nySakDto.getJournalenhet().ifPresent(sakResource::addJournalenhet);
        nySakDto.getAdministrativenhet().ifPresent(sakResource::addAdministrativEnhet);
        nySakDto.getSaksansvarlig().ifPresent(sakResource::addSaksansvarlig);
        nySakDto.getArkivdel().ifPresent(sakResource::addArkivdel);
        nySakDto.getKlasse()
                .map(klasseMappingService::toKlasse)
                .ifPresent(sakResource::setKlasse);
        nySakDto.getSkjerming()
                .map(skjermingMappingService::toSkjermingResource)
                .ifPresent(sakResource::setSkjerming);
        return sakResource;
    }

}
