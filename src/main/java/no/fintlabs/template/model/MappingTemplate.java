package no.fintlabs.template.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
public class MappingTemplate {

    private final String displayName;

    @NotNull
    @Valid
    private final ObjectTemplate rootObjectTemplate;

}
