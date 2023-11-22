package no.fintlabs.dispatch.file;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.dispatch.DispatchStatus;
import no.fintlabs.dispatch.file.result.FileDispatchResult;
import no.fintlabs.dispatch.file.result.FilesDispatchResult;
import no.fintlabs.model.instance.DokumentobjektDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
public class FilesDispatchService {

    private final FileDispatchService fileDispatchService;

    private final FilesWarningMessageService filesWarningMessageService;

    public FilesDispatchService(
            FileDispatchService fileDispatchService,
            FilesWarningMessageService filesWarningMessageService
    ) {
        this.fileDispatchService = fileDispatchService;
        this.filesWarningMessageService = filesWarningMessageService;
    }

    public Mono<FilesDispatchResult> dispatch(Collection<DokumentobjektDto> dokumentobjektDtos) {
        log.info("Dispatching files");
        if (dokumentobjektDtos.isEmpty()) {
            return Mono.just(FilesDispatchResult.accepted(Map.of()));
        }
        return Flux.fromStream(dokumentobjektDtos.stream())
                .concatMap(fileDispatchService::dispatch)
                .takeUntil(fileDispatchResult -> fileDispatchResult.getStatus() != DispatchStatus.ACCEPTED)
                .collectList()
                .flatMap(fileDispatchResults -> {

                    FileDispatchResult lastResult = fileDispatchResults.get(fileDispatchResults.size() - 1);
                    DispatchStatus lastStatus = lastResult.getStatus();
                    List<FileDispatchResult> successfulFileDispatches = lastStatus == DispatchStatus.ACCEPTED
                            ? fileDispatchResults
                            : fileDispatchResults.subList(0, fileDispatchResults.size() - 1);

                    if (lastStatus == DispatchStatus.ACCEPTED) {
                        return Mono.just(FilesDispatchResult.accepted(
                                successfulFileDispatches
                                        .stream()
                                        .collect(toMap(
                                                FileDispatchResult::getFileId,
                                                FileDispatchResult::getArchiveFileLink
                                        ))
                        ));
                    }

                    return (successfulFileDispatches.isEmpty()
                            ? Mono.just(Optional.<String>empty())
                            : filesWarningMessageService.createFunctionalWarningMessage(
                            successfulFileDispatches
                                    .stream()
                                    .map(FileDispatchResult::getArchiveFileLink)
                                    .toList())
                    ).map(warningMessageOptional -> {
                        if (lastStatus == DispatchStatus.DECLINED) {
                            return FilesDispatchResult.declined(
                                    lastResult.getErrorMessage(),
                                    warningMessageOptional.orElse(null)
                            );
                        } else {
                            return FilesDispatchResult.failed(
                                    warningMessageOptional.orElse(null)
                            );
                        }
                    });

                }).doOnNext(result -> log.info("Dispatch result=" + result.toString()));
    }

}
