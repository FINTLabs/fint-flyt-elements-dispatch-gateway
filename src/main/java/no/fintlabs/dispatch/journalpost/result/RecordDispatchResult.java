package no.fintlabs.dispatch.journalpost.result;

import lombok.*;
import no.fintlabs.dispatch.DispatchStatus;

import java.util.Optional;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecordDispatchResult {

    public static RecordDispatchResult accepted(Long journalpostId) {
        return new RecordDispatchResult(DispatchStatus.ACCEPTED, journalpostId, null, null);
    }

    public static RecordDispatchResult declined(String errorMessage, String functionalWarningMessage) {
        return new RecordDispatchResult(DispatchStatus.DECLINED, null, errorMessage, functionalWarningMessage);
    }

    public static RecordDispatchResult failed(String errorMessage, String functionalWarningMessage) {
        return new RecordDispatchResult(DispatchStatus.FAILED, null, errorMessage, functionalWarningMessage);
    }

    private final DispatchStatus status;
    private final Long journalpostId;
    private final String errorMessage;
    private final String functionalWarningMessage;

    public Optional<String> getFunctionalWarningMessage() {
        return Optional.ofNullable(functionalWarningMessage);
    }

}
