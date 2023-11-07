package no.fintlabs.dispatch.file.result;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import no.fint.model.resource.Link;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileDispatchResult {

    public enum Status {
        ACCEPTED, DECLINED, COULD_NOT_BE_RETRIEVED, FAILED
    }

    public static FileDispatchResult accepted(UUID fileId, Link archiveFileLink) {
        return new FileDispatchResult(Status.ACCEPTED, fileId, archiveFileLink, null);
    }

    public static FileDispatchResult declined(UUID fileId, String errorMessage) {
        return new FileDispatchResult(Status.DECLINED, fileId, null, errorMessage);
    }

    public static FileDispatchResult couldNotBeRetrieved(UUID fileId) {
        return new FileDispatchResult(Status.COULD_NOT_BE_RETRIEVED, fileId, null, null);
    }

    public static FileDispatchResult noFileId() {
        return new FileDispatchResult(Status.FAILED, null, null, "No fileId");
    }

    public static FileDispatchResult failed(UUID fileId) {
        return new FileDispatchResult(Status.FAILED, fileId, null, null);
    }

    private final Status status;
    private final UUID fileId;
    private final Link archiveFileLink;
    private final String errorMessage;

}
