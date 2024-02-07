package no.fintlabs.flyt.gateway.application.archive.resource.configuration;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class PullConfiguration {
    private long initialDelayMs;
    private long fixedDelayMs;
}
