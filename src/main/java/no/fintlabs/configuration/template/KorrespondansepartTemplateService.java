package no.fintlabs.configuration.template;

import no.fintlabs.configuration.template.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KorrespondansepartTemplateService {

    private final AdresseTemplateService adresseTemplateService;
    private final KontaktinformasjonTemplateService kontaktinformasjonTemplateService;
    private final SkjermingTemplateService skjermingTemplateService;

    public KorrespondansepartTemplateService(
            AdresseTemplateService adresseTemplateService,
            KontaktinformasjonTemplateService kontaktinformasjonTemplateService,
            SkjermingTemplateService skjermingTemplateService
    ) {
        this.adresseTemplateService = adresseTemplateService;
        this.kontaktinformasjonTemplateService = kontaktinformasjonTemplateService;
        this.skjermingTemplateService = skjermingTemplateService;
    }

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("korrespondanseparttype")
                                .displayName("Korrespondanseparttype")
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/skjermingshjemmel").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("organisasjonsnummer")
                                .displayName("Organisasjonsnummer")
                                .description("")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("fodselsnummer")
                                .displayName("Fødselsnummer")
                                .description("")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("navn")
                                .displayName("Navn")
                                .description("")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("adresse")
                                .displayName("Adresse")
                                .description("")
                                .build(),
                        adresseTemplateService.createTemplate()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("kontaktinformasjon")
                                .displayName("Kontaktinformasjon")
                                .description("")
                                .build(),
                        kontaktinformasjonTemplateService.createTemplate()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("skjerming")
                                .displayName("Skjerming")
                                .description("")
                                .build(),
                        skjermingTemplateService.createTemplate()
                )
                .build();
    }

}
