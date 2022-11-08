package no.fintlabs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class File {
    private UUID id;
    private String name;
    private String type;
    private String encoding;
    @JsonProperty(value = "contents")
    private String base64Contents;
}
