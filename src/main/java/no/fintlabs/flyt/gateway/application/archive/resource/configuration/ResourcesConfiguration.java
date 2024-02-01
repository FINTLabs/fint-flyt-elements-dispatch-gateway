package no.fintlabs.flyt.gateway.application.archive.resource.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties("fint.flyt.gateway.application.archive.resource")
public class ResourcesConfiguration {
    private RefreshConfiguration refresh;
    private PullConfiguration pull;
    private List<EntityPipelineConfiguration> entityPipelines;
}
