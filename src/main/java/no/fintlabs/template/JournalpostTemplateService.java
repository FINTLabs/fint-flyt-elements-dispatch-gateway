package no.fintlabs.template;

import no.fintlabs.template.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JournalpostTemplateService {

    private final KorrespondansepartTemplateService korrespondansepartTemplateService;
    private final DokumentbeskrivelseTemplateService dokumentbeskrivelseTemplateService;
    private final SkjermingTemplateService skjermingTemplateService;


    public JournalpostTemplateService(
            KorrespondansepartTemplateService korrespondansepartTemplateService,
            DokumentbeskrivelseTemplateService dokumentbeskrivelseTemplateService,
            SkjermingTemplateService skjermingTemplateService
    ) {
        this.korrespondansepartTemplateService = korrespondansepartTemplateService;
        this.dokumentbeskrivelseTemplateService = dokumentbeskrivelseTemplateService;
        this.skjermingTemplateService = skjermingTemplateService;
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
                                .key("offentligTittel")
                                .displayName("Offentlig tittel")
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
                                .key("journalposttype")
                                .displayName("Journalposttype")
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/journalposttype").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("administrativEnhet")
                                .displayName("Administrativ enhet")
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/administrativenhet").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("saksbehandler")
                                .displayName("Saksbehandler")
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/arkivressurs").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("journalstatus")
                                .displayName("Journalstatus")
                                .description("")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/journalstatus").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("skjerming")
                                .displayName("Skjerming")
                                .description("")
                                .build(),
                        skjermingTemplateService.createTemplate()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("korrespondansepart")
                                .displayName("Korrespondanseparter")
                                .description("")
                                .build(),
                        ObjectCollectionTemplate
                                .builder()
                                .elementTemplate(korrespondansepartTemplateService.createTemplate())
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("dokumentbeskrivelse")
                                .displayName("Dokumentbeskrivelser")
                                .description("")
                                .build(),
                        ObjectCollectionTemplate
                                .builder()
                                .elementTemplate(dokumentbeskrivelseTemplateService.createTemplate())
                                .build()
                )
                .build();
    }

}
