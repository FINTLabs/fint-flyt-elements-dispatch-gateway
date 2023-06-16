package no.fintlabs.template;

import no.fintlabs.template.model.*;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SakTemplateService {

    private final KlasseringTemplateService klasseringTemplateService;
    private final SkjermingTemplateService skjermingTemplateService;
    private final JournalpostTemplateService journalpostTemplateService;
    private final PartTemplateService partTemplateService;

    public SakTemplateService(
            KlasseringTemplateService klasseringTemplateService,
            SkjermingTemplateService skjermingTemplateService,
            JournalpostTemplateService journalpostTemplateService,
            PartTemplateService partTemplateService
    ) {
        this.klasseringTemplateService = klasseringTemplateService;
        this.skjermingTemplateService = skjermingTemplateService;
        this.journalpostTemplateService = journalpostTemplateService;
        this.partTemplateService = partTemplateService;
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
                                .description("Offentlig tittel på arkivenheten, ord som skal skjermes er fjernet " +
                                        "fra innholdet i tittelen (erstattet med ******)")
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("saksmappetype")
                                .displayName("Saksmappetype")
                                .description("Saksmappetyper brukes for å differensiere innhold og etter hvilke rutiner saker skal behandles.")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/saksmappetype").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("journalenhet")
                                .displayName("Journalenhet")
                                .description("Navn på enhet som har det arkivmessige ansvaret for kvalitetssikring av" +
                                        "arkivdanningen, og eventuelt registrering (journalføring) og arkivering" +
                                        "av fysiske dokumenter")
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
                                .key("administrativEnhet")
                                .displayName("Administrativ enhet")
                                .description("Navn på avdeling, kontor eller annen administrativ enhet som har" +
                                        "ansvaret for saksbehandlingen.")
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
                                .key("saksansvarlig")
                                .displayName("Saksansvarlig")
                                .description("Navn på person som er saksansvarlig")
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
                                .key("arkivdel")
                                .displayName("Arkivdel")
                                .description("Arkivdel som mappe tilhører")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/arkivdel").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("saksstatus")
                                .displayName("Saksstatus")
                                .description("Status til saksmappen, dvs. hvor langt saksbehandlingen har kommet.")
                                .build(),
                        SelectableValueTemplate
                                .builder()
                                .type(SelectableValueTemplate.Type.DYNAMIC_STRING_OR_SEARCH_SELECT)
                                .selectablesSources(List.of(
                                        UrlBuilder.builder().urlTemplate("api/intern/arkiv/kodeverk/sakstatus").build()
                                ))
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("part")
                                .displayName("Parter")
                                .description("Part til Mappe, Registrering eller Dokumentbeskrivelse.")
                                .build(),
                        ObjectCollectionTemplate
                                .builder()
                                .elementTemplate(partTemplateService.createTemplate())
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("skjerming")
                                .displayName("Skjerming")
                                .description("Skjerming benyttes til å skjerme registrerte opplysninger eller " +
                                        "enkeltdokumenter. Skjermingen trer i kraft når en tilgangskode påføres " +
                                        "den enkelte mappe, registrering eller det enkelte dokument.")
                                .build(),
                        skjermingTemplateService.createTemplate()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("klasse")
                                .displayName("Klassering")
                                .description("Klassifisering av mappe")
                                .build(),
                        ObjectCollectionTemplate
                                .builder()
                                .elementTemplate(klasseringTemplateService.createTemplate())
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("journalpost")
                                .displayName("Journalposter")
                                .description("En journalpost representer en \"innføring i journalen\"." +
                                        "Journalen er en kronologisk fortegnelse over inn- og utgående dokumenter" +
                                        "(dvs. korrespondansedokumenter) brukt i saksbehandlingen, og eventuelt også " +
                                        "interne dokumenter.")
                                .build(),
                        ObjectCollectionTemplate
                                .builder()
                                .elementTemplate(journalpostTemplateService.createTemplate())
                                .build()
                )
                .build();
    }
}
