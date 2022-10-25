package no.fintlabs.model.mappedinstance;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@NoArgsConstructor
public class MappedInstanceCollectionField {

    public enum Type {
        STRING, URL
    }

    private String key;
    private Type type;
    private Collection<String> values;

}
