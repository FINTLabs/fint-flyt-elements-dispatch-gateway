package no.fintlabs.dispatch.sak.result;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CaseDispatchResult {

    public enum Status {
        ACCEPTED, DECLINED, FAILED
    }

    public static CaseDispatchResult accepted(String archiveCaseId) {
        return new CaseDispatchResult(Status.ACCEPTED, archiveCaseId, null);
    }

    public static CaseDispatchResult declined(String errorMessage) {
        return new CaseDispatchResult(Status.DECLINED, null, errorMessage);
    }

    public static CaseDispatchResult failed() {
        return new CaseDispatchResult(Status.FAILED, null, null);
    }

    private final Status status;
    private final String archiveCaseId;
    private final String errorMessage;

}
