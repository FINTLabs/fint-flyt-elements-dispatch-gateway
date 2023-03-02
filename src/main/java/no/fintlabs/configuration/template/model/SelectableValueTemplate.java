package no.fintlabs.configuration.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.Collection;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SelectableValueTemplate {

    public enum Type {
        DYNAMIC_STRING_OR_SEARCH_SELECT, SEARCH_SELECT, DROPDOWN
    }

    private final Type type;
    private final Collection<Selectable> selectables;
    private final Collection<UrlBuilder> selectablesSources;

}
