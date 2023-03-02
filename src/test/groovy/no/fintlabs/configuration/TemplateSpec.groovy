package no.fintlabs.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import no.fintlabs.configuration.template.*
import no.fintlabs.configuration.template.model.ObjectTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
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
        ObjectMapper.class
])
class TemplateSpec extends Specification {

    @Autowired
    ArchiveTemplateService archiveTemplateService

    @Autowired
    ObjectMapper objectMapper

    def 'should create template'() {
        when:
        ObjectTemplate template = archiveTemplateService.createTemplate()
        String templateJson = objectMapper.writeValueAsString(template)
        ObjectTemplate readTemplate = objectMapper.readValue(templateJson, ObjectTemplate.class)

        then:
        template == readTemplate
    }

}
