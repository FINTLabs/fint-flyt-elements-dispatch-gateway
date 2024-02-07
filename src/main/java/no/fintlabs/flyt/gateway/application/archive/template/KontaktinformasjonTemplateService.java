package no.fintlabs.flyt.gateway.application.archive.template;

import no.fintlabs.flyt.gateway.application.archive.template.model.ElementConfig;
import no.fintlabs.flyt.gateway.application.archive.template.model.ObjectTemplate;
import no.fintlabs.flyt.gateway.application.archive.template.model.ValueTemplate;
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
                                .description("E-postadresse")
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
                                .description("Mobiltelefonnummer")
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
                                .description("Telefonnummer")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )

                .build();
    }

}
