package no.fintlabs.flyt.gateway.application.archive.template;

import no.fintlabs.flyt.gateway.application.archive.template.model.ElementConfig;
import no.fintlabs.flyt.gateway.application.archive.template.model.ValueTemplate;
import no.fintlabs.flyt.gateway.application.archive.template.model.ObjectTemplate;
import org.springframework.stereotype.Service;

@Service
public class AdresseTemplateService {

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
                .addCollectionTemplate(
                        ElementConfig
                                .builder()
                                .key("adresselinje")
                                .displayName("Adresselinjer")
                                .description("Adresseinformasjon. Linjer representeres hver for seg, fra øverst til " +
                                        "nederst. \nDette kan være: Gateadresse, Postboksadresse, Bolignummer, " +
                                        "C/O adresse, Attn, Mottak på vegne av andre.")
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
