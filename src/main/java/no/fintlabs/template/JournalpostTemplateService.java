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
                                .description("Tittel eller navn på arkivenheten")
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
                                .description("Offentlig tittel på arkivenheten, ord som skal skjermes er fjernet fra " +
                                        "innholdet i tittelen (erstattet med ******)")
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
                                .description("Navn på type journalpost. Tilsvarer \"Noark dokumenttype\" i Noark 4")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
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
                                .description("Navn på avdeling, kontor eller annen administrativ enhet som har" +
                                        "ansvaret for saksbehandlingen")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
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
                                .description("Navn på person som er saksbehandler")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
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
                                .description("Status til journalposten, dvs. om dokumentet er registrert, under" +
                                        "behandling eller endelig arkivert.")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
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
                                .description("Skjerming av registrering")
                                .build(),
                        skjermingTemplateService.createTemplate()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("korrespondansepart")
                                .displayName("Korrespondanseparter")
                                .description("Mottaker eller sender av arkivdokumenter.")
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
                                .description("Dokumentbeskrivelsene til en registrering")
                                .build(),
                        ObjectCollectionTemplate
                                .builder()
                                .elementTemplate(dokumentbeskrivelseTemplateService.createTemplate())
                                .build()
                )
                .build();
    }

}
