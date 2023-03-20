package no.fintlabs.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import no.fintlabs.configuration.template.*
import no.fintlabs.configuration.template.model.MappingTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Ignore
import spock.lang.Specification

@ContextConfiguration(classes = [
        AdresseTemplateService.class,
        ArchiveTemplateService.class,
        DokumentbeskrivelseTemplateService.class,
        DokumentobjektTemplateService.class,
        JournalpostTemplateService.class,
        KlasseringTemplateService.class,
        KontaktinformasjonTemplateService.class,
        KorrespondansepartTemplateService.class,
        NySakTemplateService.class,
        SakTemplateService.class,
        SkjermingTemplateService.class,
        PartTemplateService.class,
        ObjectMapper.class
])
class MappingTemplateSpec extends Specification {

    @Autowired
    ArchiveTemplateService archiveTemplateService

    @Autowired
    ObjectMapper objectMapper

    @Ignore
    def 'should create template'() {
        when:
        MappingTemplate template = archiveTemplateService.createTemplate()
        String templateJson = objectMapper.writeValueAsString(template)
        then:
        template == readTemplate
    }

}
