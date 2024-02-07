package no.fintlabs.flyt.gateway.application.archive.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.Valid;
import java.util.Collection;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dependency {
    private final Collection<Collection<@Valid ValuePredicate>> hasAnyCombination;
}
