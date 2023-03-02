package no.fintlabs.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import no.fintlabs.configuration.template.*
import no.fintlabs.configuration.template.model.ObjectTemplate
import spock.lang.Specification

class TemplateSpec extends Specification {


    ArchiveTemplateService archiveTemplateService
    ObjectMapper objectMapper

    def setup() {
        archiveTemplateService = new ArchiveTemplateService(
                new SakTemplateService(
                        new KlasseringTemplateService(),
                        new SkjermingTemplateService(),
                        new JournalpostTemplateService(
                                new KorrespondansepartTemplateService(
                                        new AdresseTemplateService(),
                                        new KontaktinformasjonTemplateService(),
                                        new SkjermingTemplateService()
                                ),
                                new DokumentbeskrivelseTemplateService(
                                        new DokumentobjektTemplateService()
                                ),
                                new SkjermingTemplateService()
                        )
                )
        )
        objectMapper = new ObjectMapper()
    }

    def 'should create template'() {
        when:
        ObjectTemplate template = archiveTemplateService.createTemplate()
        String templateJson = objectMapper.writeValueAsString(template)
        ObjectTemplate readTemplate = objectMapper.readValue(templateJson, ObjectTemplate.class)

        then:
        template == readTemplate
    }

}
