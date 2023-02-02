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
        sakResource.setTittel(nySakDto.getTittel());
        sakResource.setOffentligTittel(nySakDto.getOffentligTittel());
        sakResource.addSaksmappetype(nySakDto.getSaksmappetype());
        sakResource.addSaksstatus(nySakDto.getSaksstatus());
        sakResource.addJournalenhet(nySakDto.getJournalenhet());
        sakResource.addAdministrativEnhet(nySakDto.getAdministrativenhet());
        sakResource.addSaksansvarlig(nySakDto.getSaksansvarlig());
        sakResource.addArkivdel(nySakDto.getArkivdel());
        sakResource.setKlasse(klasseMappingService.toKlasse(nySakDto.getKlasse()));
        sakResource.setSkjerming(skjermingMappingService.toSkjermingResource(nySakDto.getSkjerming()));
        return sakResource;
    }

}
