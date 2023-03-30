package no.fintlabs.template;

import no.fintlabs.template.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DokumentbeskrivelseTemplateService {

    private final DokumentobjektTemplateService dokumentobjektTemplateService;

    public DokumentbeskrivelseTemplateService(DokumentobjektTemplateService dokumentobjektTemplateService) {
        this.dokumentobjektTemplateService = dokumentobjektTemplateService;
    }

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("tittel")
                                .displayName("Tittel")
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
                                .key("dokumentstatus")
                                .displayName("Dokumentstatus")
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder
                                                .builder()
                                                .urlTemplate("api/intern/arkiv/kodeverk/dokumentstatus")
                                                .build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("dokumentType")
                                .displayName("Dokumenttype")
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder
                                                .builder()
                                                .urlTemplate("api/intern/arkiv/kodeverk/dokumenttype")
                                                .build()
                                ))
                                .build()

                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("tilknyttetRegistreringSom")
                                .displayName("Tilknyttet registrering som")
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder
                                                .builder()
                                                .urlTemplate("api/intern/arkiv/kodeverk/tilknyttetregistreringsom")
                                                .build()
                                ))
                                .build()

                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("dokumentobjekt")
                                .displayName("Dokumentobjekter")
                                .description("")
                                .build(),
                        ObjectCollectionTemplate
                                .builder()
                                .elementTemplate(dokumentobjektTemplateService.createTemplate())
                                .build()
                )
                .build();
    }

}
