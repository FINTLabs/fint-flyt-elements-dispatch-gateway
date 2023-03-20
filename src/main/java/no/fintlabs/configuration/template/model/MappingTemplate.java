package no.fintlabs.configuration.template.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
public class MappingTemplate {
    private final String displayName;
    private final ObjectTemplate rootObjectTemplate;
}
