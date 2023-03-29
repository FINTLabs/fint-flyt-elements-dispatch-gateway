package no.fintlabs.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import no.fintlabs.template.*
import no.fintlabs.template.model.MappingTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Ignore
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

@ContextConfiguration(classes = [
        AdresseTemplateService.class,
        ArchiveTemplateService.class,
        DokumentbeskrivelseTemplateService.class,
        DokumentobjektTemplateService.class,
        JournalpostTemplateService.class,
        KlasseringTemplateService.class,
        KontaktinformasjonTemplateService.class,
        KorrespondansepartTemplateService.class,
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

    Validator validator

    def setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory()
        validator = factory.getValidator()
    }

    @Ignore
    def 'should create template'() {
        when:
        MappingTemplate template = archiveTemplateService.createTemplate()
        Set<ConstraintViolation<MappingTemplate>> constraintViolations = validator.validate(template)
        String templateJson = objectMapper.writeValueAsString(template)
        then:
        template == readTemplate
    }

}
