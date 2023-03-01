package no.fintlabs.configuration.template.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collection;

@Getter
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ObjectTemplate {

    public static ObjectTemplateBuilder builder() {
        return new ObjectTemplateBuilder();
    }

    @JsonCreator
    public ObjectTemplate(
            @JsonProperty("valueTemplates") Collection<ElementTemplate<ValueTemplate>> valueTemplates,
            @JsonProperty("selectableValueTemplates") Collection<ElementTemplate<SelectableValueTemplate>> selectableValueTemplates,
            @JsonProperty("objectTemplates") Collection<ElementTemplate<ObjectTemplate>> objectTemplates,
            @JsonProperty("objectCollectionTemplates") Collection<ElementTemplate<ObjectCollectionTemplate>> objectCollectionTemplates
    ) {
        this.valueTemplates = valueTemplates;
        this.selectableValueTemplates = selectableValueTemplates;
        this.objectTemplates = objectTemplates;
        this.objectCollectionTemplates = objectCollectionTemplates;
    }

    private final Collection<ElementTemplate<ValueTemplate>> valueTemplates;
    private final Collection<ElementTemplate<SelectableValueTemplate>> selectableValueTemplates;
    private final Collection<ElementTemplate<ObjectTemplate>> objectTemplates;
    private final Collection<ElementTemplate<ObjectCollectionTemplate>> objectCollectionTemplates;

}
