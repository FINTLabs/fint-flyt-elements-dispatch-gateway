package no.fintlabs.template;

import no.fintlabs.template.model.ElementConfig;
import no.fintlabs.template.model.ObjectTemplate;
import no.fintlabs.template.model.ValueTemplate;
import org.springframework.stereotype.Service;

@Service
public class AdresseTemplateService {

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("adresselinje")
                                .displayName("Adresselinje")
                                .description("Postadressen til en avsender/mottaker eller part")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("postnummer")
                                .displayName("Postnummer")
                                .description("Postnummer")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("poststed")
                                .displayName("Poststed")
                                .description("Poststed")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .build();
    }

}
