package no.fintlabs.configuration.template;

import no.fintlabs.configuration.template.model.ElementConfig;
import no.fintlabs.configuration.template.model.ObjectTemplate;
import no.fintlabs.configuration.template.model.SelectableValueTemplate;
import no.fintlabs.configuration.template.model.UrlBuilder;
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
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.SEARCH_SELECT)
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
                .build();
    }

}
