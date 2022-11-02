package no.fintlabs.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result {

    public static Result accepted(String archiveCaseId) {
        return new Result(Status.ACCEPTED, archiveCaseId, null);
    }

    public static Result declined(String errorMessage) {
        return new Result(Status.DECLINED, null, errorMessage);
    }

    public static Result failed() {
        return new Result(Status.FAILED, null, null);
    }

    private final Status status;
    private final String archiveCaseId;
    private final String errorMessage;

}
