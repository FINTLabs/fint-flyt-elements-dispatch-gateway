package no.fintlabs.flyt.gateway.application.archive.dispatch.journalpost.result;

import lombok.*;
import no.fintlabs.flyt.gateway.application.archive.dispatch.DispatchStatus;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecordDispatchResult {

    public static RecordDispatchResult accepted(Long journalpostId) {
        return new RecordDispatchResult(DispatchStatus.ACCEPTED, journalpostId, null);
    }

    public static RecordDispatchResult declined(String errorMessage) {
        return new RecordDispatchResult(DispatchStatus.DECLINED, null, errorMessage);
    }

    public static RecordDispatchResult failed(String errorMessage) {
        return new RecordDispatchResult(DispatchStatus.FAILED, null, errorMessage);
    }

    private final DispatchStatus status;
    private final Long journalpostId;
    private final String errorMessage;


}
