package no.fintlabs.dispatch.journalpost.result;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecordsDispatchResult {

    public enum Status {
        ACCEPTED, DECLINED, FAILED
    }

    public static RecordsDispatchResult accepted(List<RecordDispatchResult> successfulRecordDispatchResults) {
        return new RecordsDispatchResult(Status.ACCEPTED, successfulRecordDispatchResults, null);
    }

    public static RecordsDispatchResult declined(
            List<RecordDispatchResult> successfulRecordDispatchResults,
            RecordDispatchResult failedRecordDispatchResult
    ) {
        return new RecordsDispatchResult(Status.DECLINED, successfulRecordDispatchResults, failedRecordDispatchResult);
    }

    public static RecordsDispatchResult failed(
            List<RecordDispatchResult> successfulRecordDispatchResults,
            RecordDispatchResult failedRecordDispatchResult
    ) {
        return new RecordsDispatchResult(Status.FAILED, successfulRecordDispatchResults, failedRecordDispatchResult);
    }

    private final Status status;
    private final List<RecordDispatchResult> successfulRecordDispatchResults;
    private final RecordDispatchResult failedRecordDispatchResult;

}
