package no.fintlabs.template;

import no.fintlabs.template.model.MappingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ArchiveTemplateService {

    private final SakTemplateService sakTemplateService;

    public ArchiveTemplateService(SakTemplateService sakTemplateService) {
        this.sakTemplateService = sakTemplateService;
    }

    public MappingTemplate createTemplate() {
        return MappingTemplate
                .builder()
                .displayName("Arkivering")
                .rootObjectTemplate(sakTemplateService.createTemplate())
                .build();
    }

}
