package no.fintlabs.configuration.template;

import no.fintlabs.configuration.template.model.ElementConfig;
import no.fintlabs.configuration.template.model.ElementTemplate;
import no.fintlabs.configuration.template.model.ObjectTemplate;
import org.springframework.stereotype.Service;

@Service
public class ArchiveTemplateService {

    private final SakTemplateService sakTemplateService;

    public ArchiveTemplateService(SakTemplateService sakTemplateService) {
        this.sakTemplateService = sakTemplateService;
    }

    public ElementTemplate<ObjectTemplate> createTemplate() {
        return ElementTemplate
                .<ObjectTemplate>builder()
                .order(0)
                .elementConfig(
                        ElementConfig
                                .builder()
                                .key("sak")
                                .displayName("Sak")
                                .description("")
                                .build()
                )
                .template(sakTemplateService.createTemplate())
                .build();
    }
}
