package no.fintlabs.template;

import no.fintlabs.model.CaseDispatchType;
import no.fintlabs.template.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ArchiveTemplateService {

    private final SakTemplateService sakTemplateService;
    private final JournalpostTemplateService journalpostTemplateService;

    public ArchiveTemplateService(
            SakTemplateService sakTemplateService,
            JournalpostTemplateService journalpostTemplateService
    ) {
        this.sakTemplateService = sakTemplateService;
        this.journalpostTemplateService = journalpostTemplateService;
    }

    public MappingTemplate createTemplate() {
        return MappingTemplate
                .builder()
                .displayName("Arkivering")
                .rootObjectTemplate(
                        ObjectTemplate
                                .builder()
                                .addTemplate(
                                        ElementConfig
                                                .builder()
                                                .key("type")
                                                .displayName("Sakslogikk")
                                                .description("")
                                                .build(),
                                        SelectableValueTemplate
                                                .builder()
                                                .type(SelectableValueTemplate.Type.DROPDOWN)
                                                .selectables(List.of(
                                                        Selectable
                                                                .builder()
                                                                .displayName("Ny")
                                                                .value(CaseDispatchType.NEW.name())
                                                                .build(),
//                                        Selectable
//                                                .builder()
//                                                .displayName("På søk, eller ny")
//                                                .value(CaseDispatchType.BY_SEARCH_OR_NEW.name())
//                                                .build(),
                                                        Selectable
                                                                .builder()
                                                                .displayName("På saksnummer")
                                                                .value(CaseDispatchType.BY_ID.name())
                                                                .build()
                                                ))
                                                .build()
                                )
                                .addTemplate(
                                        ElementConfig
                                                .builder()
                                                .key("newCase")
                                                .displayName("Sak")
                                                .description("")
                                                .showDependency(
                                                        Dependency
                                                                .builder()
                                                                .hasAnyCombination(List.of(
                                                                        List.of(
                                                                                ValuePredicate
                                                                                        .builder()
                                                                                        .key("type")
                                                                                        .defined(true)
                                                                                        .value(CaseDispatchType.NEW.name())
                                                                                        .build()
                                                                        ),
                                                                        List.of(
                                                                                ValuePredicate
                                                                                        .builder()
                                                                                        .key("type")
                                                                                        .defined(true)
                                                                                        .value(CaseDispatchType.BY_SEARCH_OR_NEW.name())
                                                                                        .build()
                                                                        )
                                                                ))
                                                                .build()
                                                )
                                                .build(),
                                        sakTemplateService.createTemplate()
                                )
                                .addTemplate(
                                        ElementConfig
                                                .builder()
                                                .key("caseId")
                                                .displayName("Saksnummer")
                                                .description("")
                                                .showDependency(
                                                        Dependency
                                                                .builder()
                                                                .hasAnyCombination(List.of(
                                                                        List.of(
                                                                                ValuePredicate
                                                                                        .builder()
                                                                                        .key("type")
                                                                                        .defined(true)
                                                                                        .value(CaseDispatchType.BY_ID.name())
                                                                                        .build()
                                                                        )
                                                                ))
                                                                .build()
                                                )
                                                .build(),
                                        ValueTemplate
                                                .builder()
                                                .type(ValueTemplate.Type.DYNAMIC_STRING)
                                                .search(
                                                        UrlBuilder
                                                                .builder()
                                                                .urlTemplate("api/intern/arkiv/saker/{caseId}/tittel")
                                                                .valueRefPerPathParamKey(Map.of(
                                                                        "caseId", "id"
                                                                ))
                                                                .build()
                                                )
                                                .build()
                                )
                                .addTemplate(
                                        ElementConfig
                                                .builder()
                                                .key("journalpost")
                                                .displayName("Journalposter")
                                                .description("")
                                                .showDependency(
                                                        Dependency
                                                                .builder()
                                                                .hasAnyCombination(List.of(
                                                                        List.of(
                                                                                ValuePredicate
                                                                                        .builder()
                                                                                        .key("type")
                                                                                        .defined(true)
                                                                                        .value(CaseDispatchType.BY_ID.name())
                                                                                        .build()
                                                                        )
                                                                ))
                                                                .build()
                                                )
                                                .build(),
                                        ObjectCollectionTemplate
                                                .builder()
                                                .elementTemplate(journalpostTemplateService.createTemplate())
                                                .build()
                                )
                                .build()
                )
                .build();
    }

}