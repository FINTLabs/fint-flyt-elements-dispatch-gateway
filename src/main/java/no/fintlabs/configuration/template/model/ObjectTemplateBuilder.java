package no.fintlabs.configuration.template.model;

import java.util.ArrayList;
import java.util.Collection;

public class ObjectTemplateBuilder {
    private int nextOrderValue = 0;
    private final Collection<ElementTemplate<ValueTemplate>> valueTemplates = new ArrayList<>();
    private final Collection<ElementTemplate<SelectableValueTemplate>> selectableValueTemplates = new ArrayList<>();
    private final Collection<ElementTemplate<ObjectTemplate>> objectTemplates = new ArrayList<>();
    private final Collection<ElementTemplate<ObjectCollectionTemplate>> objectCollectionTemplates = new ArrayList<>();

    public ObjectTemplateBuilder addTemplate(ElementConfig elementConfig, ValueTemplate template) {
        return addTemplate(valueTemplates, elementConfig, template);
    }

    public ObjectTemplateBuilder addTemplate(ElementConfig elementConfig, SelectableValueTemplate template) {
        return addTemplate(selectableValueTemplates, elementConfig, template);
    }

    public ObjectTemplateBuilder addTemplate(ElementConfig elementConfig, ObjectTemplate template) {
        return addTemplate(objectTemplates, elementConfig, template);
    }

    public ObjectTemplateBuilder addTemplate(ElementConfig elementConfig, ObjectCollectionTemplate template) {
        return addTemplate(objectCollectionTemplates, elementConfig, template);
    }

    private <T> ObjectTemplateBuilder addTemplate(
            Collection<ElementTemplate<T>> collection,
            ElementConfig elementConfig,
            T template
    ) {
        collection.add(
                ElementTemplate
                        .<T>builder()
                        .order(nextOrderValue++)
                        .elementConfig(elementConfig)
                        .template(template)
                        .build()
        );
        return this;
    }

    public ObjectTemplate build() {
        return new ObjectTemplate(
                valueTemplates,
                selectableValueTemplates,
                objectTemplates,
                objectCollectionTemplates
        );
    }

}
