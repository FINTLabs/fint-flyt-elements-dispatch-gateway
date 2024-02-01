package no.fintlabs.flyt.gateway.application.archive.dispatch.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class File {
    private String name;
    private String type;
    private String encoding;
    private byte[] contents;
}
