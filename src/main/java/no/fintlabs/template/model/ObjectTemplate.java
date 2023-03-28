package no.fintlabs.template.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import no.fintlabs.template.validation.UniqueKeys;
import no.fintlabs.template.validation.UniqueOrders;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@UniqueKeys
@UniqueOrders
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
        this.valueTemplates = Optional.ofNullable(valueTemplates).orElse(new ArrayList<>());
        this.selectableValueTemplates = Optional.ofNullable(selectableValueTemplates).orElse(new ArrayList<>());
        this.objectTemplates = Optional.ofNullable(objectTemplates).orElse(new ArrayList<>());
        this.objectCollectionTemplates = Optional.ofNullable(objectCollectionTemplates).orElse(new ArrayList<>());
    }

    private final Collection<@NotNull @Valid ElementTemplate<ValueTemplate>> valueTemplates;
    private final Collection<@NotNull @Valid ElementTemplate<SelectableValueTemplate>> selectableValueTemplates;
    private final Collection<@NotNull @Valid ElementTemplate<ObjectTemplate>> objectTemplates;
    private final Collection<@NotNull @Valid ElementTemplate<ObjectCollectionTemplate>> objectCollectionTemplates;

}
