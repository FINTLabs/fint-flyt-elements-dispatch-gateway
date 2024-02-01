package no.fintlabs.flyt.gateway.application.archive.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElementTemplate<T> {

    @PositiveOrZero
    private final int order;

    @Valid
    @NotNull
    private final ElementConfig elementConfig;

    @Valid
    @NotNull
    private final T template;

}
