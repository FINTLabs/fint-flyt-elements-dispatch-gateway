package no.fintlabs.model;

import lombok.Data;

@Data
public class File {
    private String name;
    private String type;
    private String encoding;
    private byte[] contents;
}
