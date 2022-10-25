package no.fintlabs.model.mappedinstance;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MappedInstanceField {

    public enum Type {
        STRING, URL, BOOLEAN
    }

    private String key;
    private Type type;
    private String value;

}
