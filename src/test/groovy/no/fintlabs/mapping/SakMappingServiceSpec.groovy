package no.fintlabs.mapping

import no.fint.model.resource.arkiv.noark.SakResource
import no.fint.model.resource.arkiv.noark.SkjermingResource
import no.fintlabs.model.instance.SakDto
import no.fintlabs.model.instance.SkjermingDto
import spock.lang.Specification

class SakMappingServiceSpec extends Specification {

    SkjermingMappingService skjermingMappingService
    KlasseMappingService klasseMappingService
    SakMappingService sakMappingService

    def setup() {
        skjermingMappingService = Mock(SkjermingMappingService.class)
        klasseMappingService = Mock(KlasseMappingService.class)
        sakMappingService = new SakMappingService(skjermingMappingService, klasseMappingService, partMappingService)
    }

    def 'should map to SakResource'() {
        given:
        skjermingMappingService.toSkjermingResource(_ as SkjermingDto) >> new SkjermingResource()
        klasseMappingService.toKlasse(_ as List) >> List.of()

        SakDto nySakDto = SakDto
                .builder()
                .tittel("testSakTittel")
                .offentligTittel("testSakOffentligTittel")
                .build()

        when:
        SakResource sakResource = sakMappingService.toSakResource(nySakDto)

        then:
        sakResource.getTittel() == "testSakTittel"
        sakResource.getOffentligTittel() == "testSakOffentligTittel"
    }

}
