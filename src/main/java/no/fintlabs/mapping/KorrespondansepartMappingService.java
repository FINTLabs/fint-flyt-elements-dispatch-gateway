package no.fintlabs.mapping;

import no.fint.model.resource.arkiv.noark.KorrespondansepartResource;
import no.fintlabs.model.instance.KorrespondansepartDto;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class KorrespondansepartMappingService {

    private final AdresseMappingService adresseMappingService;
    private final KontaktinformasjonMappingService kontaktinformasjonMappingService;
    private final SkjermingMappingService skjermingMappingService;

    public KorrespondansepartMappingService(
            AdresseMappingService adresseMappingService,
            KontaktinformasjonMappingService kontaktinformasjonMappingService,
            SkjermingMappingService skjermingMappingService
    ) {
        this.adresseMappingService = adresseMappingService;
        this.kontaktinformasjonMappingService = kontaktinformasjonMappingService;
        this.skjermingMappingService = skjermingMappingService;
    }

    public List<KorrespondansepartResource> toKorrespondansepartResource(Collection<KorrespondansepartDto> korrespondansepartDto) {
        return korrespondansepartDto.stream().map(this::toKorrespondansepartResource).toList();
    }

    public KorrespondansepartResource toKorrespondansepartResource(KorrespondansepartDto korrespondansepartDto) {
        KorrespondansepartResource korrespondansepartResource = new KorrespondansepartResource();
        korrespondansepartResource.setFodselsnummer(korrespondansepartDto.getFodselsnummer());
        korrespondansepartResource.setOrganisasjonsnummer(korrespondansepartDto.getOrganisasjonsnummer());
        korrespondansepartResource.setKorrespondansepartNavn(korrespondansepartDto.getKorrespondansepartNavn());
        korrespondansepartResource.setAdresse(
                adresseMappingService.toAdresseResource(korrespondansepartDto.getAdresse())
        );
        korrespondansepartResource.addKorrespondanseparttype(korrespondansepartDto.getKorrespondanseparttype());
        korrespondansepartResource.setKontaktperson(korrespondansepartDto.getKontaktperson());
        korrespondansepartResource.setKontaktinformasjon(
                kontaktinformasjonMappingService.toKontaktinformasjon(korrespondansepartDto.getKontaktinformasjon())
        );
        korrespondansepartResource.setSkjerming(
                skjermingMappingService.toSkjermingResource(korrespondansepartDto.getSkjerming())
        );
        return korrespondansepartResource;
    }
}
