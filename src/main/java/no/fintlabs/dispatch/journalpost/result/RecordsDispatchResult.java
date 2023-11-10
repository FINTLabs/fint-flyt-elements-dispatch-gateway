package no.fintlabs.dispatch.journalpost.result;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import no.fintlabs.dispatch.DispatchStatus;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecordsDispatchResult {

    public static RecordsDispatchResult accepted(List<Long> journalpostIds) {
        return new RecordsDispatchResult(DispatchStatus.ACCEPTED, journalpostIds, null, null);
    }

    public static RecordsDispatchResult declined(
            String errorMessage,
            List<String> functionalWarningMessages
    ) {
        return new RecordsDispatchResult(DispatchStatus.DECLINED, null, errorMessage, functionalWarningMessages);
    }

    public static RecordsDispatchResult failed(
            String errorMessage,
            List<String> functionalWarningMessages
    ) {
        return new RecordsDispatchResult(DispatchStatus.FAILED, null, errorMessage, functionalWarningMessages);
    }

    private final DispatchStatus status;
    private final List<Long> journalpostIds;
    private final String errorMessage;
    private final List<String> functionalWarningMessages;

}
