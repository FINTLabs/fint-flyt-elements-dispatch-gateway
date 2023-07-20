package no.fintlabs.mapping;

import no.fint.model.resource.arkiv.noark.PartResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.fintlabs.model.instance.PartDto;
import no.fintlabs.model.instance.SakDto;
import no.fintlabs.model.instance.SkjermingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SakMappingServiceTest {

    private SkjermingMappingService skjermingMappingService;
    private KlasseMappingService klasseMappingService;
    private SakMappingService sakMappingService;
    private PartMappingService partMappingService;

    @BeforeEach
    void setup() {
        skjermingMappingService = mock(SkjermingMappingService.class);
        klasseMappingService = mock(KlasseMappingService.class);
        partMappingService = mock(PartMappingService.class);
        sakMappingService = new SakMappingService(skjermingMappingService, klasseMappingService, partMappingService);
    }

    @Test
    void shouldMapToSakResource() {
        when(skjermingMappingService.toSkjermingResource(any(SkjermingDto.class))).thenReturn(new SkjermingResource());
        when(klasseMappingService.toKlasse(anyList())).thenReturn(List.of());
        when(partMappingService.toPartResource(any(PartDto.class))).thenReturn(new PartResource());

        SakDto nySakDto = SakDto
                .builder()
                .tittel("testSakTittel")
                .offentligTittel("testSakOffentligTittel")
                .build();

        SakResource sakResource = sakMappingService.toSakResource(nySakDto);

        assertEquals("testSakTittel", sakResource.getTittel());
        assertEquals("testSakOffentligTittel", sakResource.getOffentligTittel());
    }
}
