package no.fintlabs.dispatch.sak.result;

import lombok.*;
import no.fintlabs.dispatch.DispatchStatus;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class CaseDispatchResult {

    public static CaseDispatchResult accepted(String archiveCaseId) {
        return new CaseDispatchResult(DispatchStatus.ACCEPTED, archiveCaseId, null);
    }

    public static CaseDispatchResult declined(String errorMessage) {
        return new CaseDispatchResult(DispatchStatus.DECLINED, null, errorMessage);
    }

    public static CaseDispatchResult failed() {
        return new CaseDispatchResult(DispatchStatus.FAILED, null, null);
    }

    private final DispatchStatus status;
    private final String archiveCaseId;
    private final String errorMessage;

}
