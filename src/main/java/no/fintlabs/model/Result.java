package no.fintlabs.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result {
    private final Status status;
    private final String archiveCaseId;
}
