package no.fintlabs.template;

import no.fintlabs.template.model.ElementConfig;
import no.fintlabs.template.model.ObjectTemplate;
import no.fintlabs.template.model.ValueTemplate;
import org.springframework.stereotype.Service;

@Service
public class KontaktinformasjonTemplateService {

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("epostadresse")
                                .displayName("E-post")
                                .description("E-postadressen til en avsender/mottaker eller part")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("mobiltelefonnummer")
                                .displayName("Mobiltelefonnummer")
                                .description("Mobiltelefonnummeret til en avsender/mottaker eller part")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("telefonnummer")
                                .displayName("Telefonnummer")
                                .description("Telefonnummeret til en avsender/mottaker eller part")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )

                .build();
    }

}
