package no.fintlabs.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElementConfig {

    @NotBlank
    @Pattern(regexp = "[^.]*")
    private final String key;

    @NotBlank
    private final String displayName;

    private final String description;

    @Valid
    private final Dependency showDependency;

    @Valid
    private final Dependency enableDependency;

}
