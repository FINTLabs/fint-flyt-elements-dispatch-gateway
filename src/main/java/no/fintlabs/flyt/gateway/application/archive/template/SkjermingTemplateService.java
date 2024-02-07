package no.fintlabs.flyt.gateway.application.archive.template;

import no.fintlabs.flyt.gateway.application.archive.template.model.ElementConfig;
import no.fintlabs.flyt.gateway.application.archive.template.model.ObjectTemplate;
import no.fintlabs.flyt.gateway.application.archive.template.model.SelectableValueTemplate;
import no.fintlabs.flyt.gateway.application.archive.template.model.UrlBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkjermingTemplateService {

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("tilgangsrestriksjon")
                                .displayName("Tilgangsrestriksjon")
                                .description("Angivelse av at dokumentene som tilhører arkivenheten ikke er offentlig " +
                                        "\ntilgjengelig i henhold til offentlighetsloven eller av en annen grunn")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/tilgangsrestriksjon").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("skjermingshjemmel")
                                .displayName("Skjermingshjemmel")
                                .description("Henvisning til hjemmel (paragraf) i offentlighetsloven, sikkerhetsloven" +
                                        "\neller beskyttelsesinstruksen")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/skjermingshjemmel").build()
                                ))
                                .build()
                )
                .build();
    }

}
