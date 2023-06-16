package no.fintlabs.template;

import no.fintlabs.template.model.*;
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
                                .description("Type korrespondansepart")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/korrespondanseparttype").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("organisasjonsnummer")
                                .displayName("Organisasjonsnummer")
                                .description("Organisasjonsnummer til Korrespondansepart")
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
                                .description("Fødselsnummer til Korrespondansepart")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("korrespondansepartNavn")
                                .displayName("Navn")
                                .description("Navn på person eller organisasjon som er avsender eller mottaker av dokumentet")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("kontaktperson")
                                .displayName("Kontaktperson")
                                .description("Kontaktperson hos en organisasjon som er avsender eller mottaker, eller sakspart")
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
                                .description("Adressen til korrespondanseparten")
                                .build(),
                        adresseTemplateService.createTemplate()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("kontaktinformasjon")
                                .displayName("Kontaktinformasjon")
                                .description("Kontaktinformasjon til korrespondansepart")
                                .build(),
                        kontaktinformasjonTemplateService.createTemplate()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("skjerming")
                                .displayName("Skjerming")
                                .description("Skjerming av korrespodansepart")
                                .build(),
                        skjermingTemplateService.createTemplate()
                )
                .build();
    }

}
