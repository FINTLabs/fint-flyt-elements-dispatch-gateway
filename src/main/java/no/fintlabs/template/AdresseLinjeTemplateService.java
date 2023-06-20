package no.fintlabs.template;

import no.fintlabs.template.model.ElementConfig;
import no.fintlabs.template.model.ObjectTemplate;
import no.fintlabs.template.model.ValueTemplate;
import org.springframework.stereotype.Service;

@Service
public class AdresseLinjeTemplateService {

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
                .build();
    }

}
