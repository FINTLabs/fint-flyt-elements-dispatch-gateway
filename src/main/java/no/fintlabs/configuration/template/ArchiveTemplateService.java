package no.fintlabs.configuration.template;

import no.fintlabs.configuration.template.model.ElementConfig;
import no.fintlabs.configuration.template.model.ObjectTemplate;
import org.springframework.stereotype.Service;

@Service
public class ArchiveTemplateService {

    private final SakTemplateService sakTemplateService;

    public ArchiveTemplateService(SakTemplateService sakTemplateService) {
        this.sakTemplateService = sakTemplateService;
    }

    public ObjectTemplate createTemplate() {
        return ObjectTemplate
                .builder()
                .addTemplate(
                        ElementConfig
                                .builder()
                                .key("sak")
                                .displayName("Sak")
                                .description("")
                                .build(),
                        sakTemplateService.createTemplate()
                )
                .build();
    }
}
