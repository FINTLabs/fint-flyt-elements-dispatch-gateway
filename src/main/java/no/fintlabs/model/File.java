package no.fintlabs.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@Jacksonized
public class File {
    private String name;
    private String type;
    private String encoding;
    private byte[] contents;
}
