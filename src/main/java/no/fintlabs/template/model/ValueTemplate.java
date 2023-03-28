package no.fintlabs.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValueTemplate {

    public enum Type {
        STRING, DYNAMIC_STRING, FILE
    }

    @NotNull
    private final Type type;

    @Valid
    private final UrlBuilder search;

}
