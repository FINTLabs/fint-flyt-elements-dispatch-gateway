package no.fintlabs.dispatch.file;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fintlabs.dispatch.DispatchMessageFormattingService;
import no.fintlabs.dispatch.DispatchStatus;
import no.fintlabs.dispatch.file.result.FileDispatchResult;
import no.fintlabs.dispatch.file.result.FilesDispatchResult;
import no.fintlabs.model.instance.DokumentbeskrivelseDto;
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

    private final DispatchMessageFormattingService dispatchMessageFormattingService;

    public FilesDispatchService(
            FileDispatchService fileDispatchService,
            DispatchMessageFormattingService dispatchMessageFormattingService
    ) {
        this.fileDispatchService = fileDispatchService;
        this.dispatchMessageFormattingService = dispatchMessageFormattingService;
    }

    public Mono<FilesDispatchResult> dispatch(Collection<DokumentbeskrivelseDto> dokumentbeskrivelseDtos) {
        if (dokumentbeskrivelseDtos.isEmpty()) {
            return Mono.just(FilesDispatchResult.accepted(Map.of()));
        }
        return Flux.fromStream(dokumentbeskrivelseDtos
                        .stream()
                        .map(DokumentbeskrivelseDto::getDokumentobjekt)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(Collection::stream))
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

                    return createFunctionalWarningMessage(
                            successfulFileDispatches
                                    .stream()
                                    .map(FileDispatchResult::getArchiveFileLink)
                                    .toList()
                    )
                            .map(warningMessageOptional -> {
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

                });
    }

    public Mono<Optional<String>> createFunctionalWarningMessage(Collection<Link> fileLinks) {
        if (fileLinks.isEmpty()) {
            return Mono.just(Optional.empty());
        }
        return getFileIds(fileLinks)
                .map(fileIds -> dispatchMessageFormattingService.createFunctionalWarningMessage(
                        "dokumentobjekt",
                        "id",
                        fileIds
                ))
                .onErrorResume(e -> {
                    log.error("Unable to get fileIds", e);
                    return Mono.just(dispatchMessageFormattingService.createFunctionalWarningMessage(
                            "dokumentobjekt",
                            "link",
                            fileLinks.stream()
                                    .map(Link::getHref)
                                    .toList()
                    ));
                })
                .doOnNext(result -> log.info("Dispatch result=" + result.toString()));
    }

    private Mono<List<String>> getFileIds(Collection<Link> fileLinks) {
        return Flux.fromIterable(fileLinks)
                .concatMap(fileDispatchService::getFileId)
                .collectList();
    }

}
