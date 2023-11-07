package no.fintlabs.dispatch.file.result;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FilesDispatchResult {

    public enum Status {
        ACCEPTED, DECLINED, FAILED
    }

    public static FilesDispatchResult accepted(
            List<FileDispatchResult> successfulFileDispatches
    ) {
        return new FilesDispatchResult(Status.ACCEPTED, successfulFileDispatches, null);
    }

    public static FilesDispatchResult declined(
            List<FileDispatchResult> successfulFileDispatches,
            FileDispatchResult failedFileDispatch
    ) {
        return new FilesDispatchResult(Status.DECLINED, successfulFileDispatches, failedFileDispatch);
    }

    public static FilesDispatchResult failed(
            List<FileDispatchResult> successfulFileDispatches,
            FileDispatchResult failedFileDispatch
    ) {
        return new FilesDispatchResult(Status.FAILED, successfulFileDispatches, failedFileDispatch);
    }

    private final Status status;
    private final List<FileDispatchResult> successfulFileDispatches;
    private final FileDispatchResult failedFileDispatch;

}
