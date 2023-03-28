package no.fintlabs.template;

import no.fintlabs.template.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartTemplateService {

    private final AdresseTemplateService adresseTemplateService;
    private final KontaktinformasjonTemplateService kontaktinformasjonTemplateService;

    public PartTemplateService(
            AdresseTemplateService adresseTemplateService,
            KontaktinformasjonTemplateService kontaktinformasjonTemplateService
    ) {
        this.adresseTemplateService = adresseTemplateService;
        this.kontaktinformasjonTemplateService = kontaktinformasjonTemplateService;
    }

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
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
                                .key("kontaktperson")
                                .displayName("Kontaktperson")
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
                                .key("partNavn")
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
                                .key("partRolle")
                                .displayName("Rolle")
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/partrolle").build()
                                ))
                                .build()
                )
                .build();
    }
}
