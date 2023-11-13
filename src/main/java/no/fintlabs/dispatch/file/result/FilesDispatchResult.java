package no.fintlabs.dispatch.file.result;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import no.fint.model.resource.Link;
import no.fintlabs.dispatch.DispatchStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class FilesDispatchResult {


    public static FilesDispatchResult accepted(
            Map<UUID, Link> archiveFileLinkPerFileId
    ) {
        return new FilesDispatchResult(DispatchStatus.ACCEPTED, archiveFileLinkPerFileId, null, null);
    }

    public static FilesDispatchResult declined(
            String errorMessage,
            String functionalWarningMessage
    ) {
        return new FilesDispatchResult(DispatchStatus.DECLINED, null, errorMessage, functionalWarningMessage);
    }

    public static FilesDispatchResult failed(
            String functionalWarningMessage
    ) {
        return new FilesDispatchResult(DispatchStatus.FAILED, null, null, functionalWarningMessage);
    }

    private final DispatchStatus status;
    private final Map<UUID, Link> archiveFileLinkPerFileId;
    private final String errorMessage;
    private final String functionalWarningMessage;

    public Optional<String> getFunctionalWarningMessage() {
        return Optional.ofNullable(functionalWarningMessage);
    }

}
