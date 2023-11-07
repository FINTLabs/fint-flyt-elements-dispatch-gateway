package no.fintlabs.dispatch.journalpost.result;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import no.fintlabs.dispatch.file.result.FilesDispatchResult;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecordDispatchResult {

    public enum Status {
        ACCEPTED, DECLINED, FILE_DECLINED, FAILED
    }

    public static RecordDispatchResult accepted(Long journalpostId, FilesDispatchResult filesDispatchResult) {
        return new RecordDispatchResult(Status.ACCEPTED, journalpostId, filesDispatchResult, null);
    }

    public static RecordDispatchResult declined(String errorMessage, FilesDispatchResult filesDispatchResult) {
        return new RecordDispatchResult(Status.DECLINED, null, filesDispatchResult, errorMessage);
    }

    public static RecordDispatchResult fileDeclined(FilesDispatchResult filesDispatchResult) {
        return new RecordDispatchResult(Status.FILE_DECLINED, null, filesDispatchResult, null);
    }

    public static RecordDispatchResult failed(FilesDispatchResult filesDispatchResult) {
        return new RecordDispatchResult(Status.FAILED, null, filesDispatchResult, null);
    }

    private final Status status;
    private final Long journalpostId;
    private final FilesDispatchResult filesDispatchResult;
    private final String errorMessage;

}
