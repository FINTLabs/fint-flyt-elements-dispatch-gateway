package no.fintlabs.configuration.template;

import no.fintlabs.configuration.template.model.*;
import no.fintlabs.model.CaseDispatchType;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class SakTemplateService {

    private final NySakTemplateService nySakTemplateService;

    public SakTemplateService(NySakTemplateService nySakTemplateService) {
        this.nySakTemplateService = nySakTemplateService;
    }

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
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
                                .selectables(
                                        Arrays.stream(CaseDispatchType.values())
                                                .map(Enum::toString)
                                                .map(enumString -> Selectable
                                                        .builder()
                                                        .displayName(enumString)
                                                        .value(enumString)
                                                        .build()
                                                )
                                                .toList()
                                )
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("id")
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
                                                                        .value("BY_ID")
                                                                        .build()
                                                        )
                                                ))
                                                .build()
                                )
                                .build(),
                        ValueTemplate
                                .builder()
                                .type(ValueTemplate.Type.STRING)
                                .search(
                                        UrlBuilder
                                                .builder()
                                                .urlTemplate("api/intern/arkiv/saker/{caseId}/tittel")
                                                .valueKeyPerPathParamKey(Map.of(
                                                        "id", "caseId"
                                                ))
                                                .build()
                                )
                                .build()
                )
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("new")
                                .displayName("Ny sak")
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
                                                                        .value("NEW")
                                                                        .build()
                                                        ),
                                                        List.of(
                                                                ValuePredicate
                                                                        .builder()
                                                                        .key("type")
                                                                        .defined(true)
                                                                        .value("BY_SEARCH_OR_NEW")
                                                                        .build()
                                                        )
                                                ))
                                                .build()
                                )
                                .build(),
                        nySakTemplateService.createTemplate()
                )
                .build();
    }
}
