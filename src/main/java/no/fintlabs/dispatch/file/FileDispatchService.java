package no.fintlabs.dispatch.file;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentfilResource;
import no.fintlabs.dispatch.DispatchStatus;
import no.fintlabs.dispatch.file.result.FileDispatchResult;
import no.fintlabs.dispatch.file.result.FilesDispatchResult;
import no.fintlabs.model.instance.DokumentbeskrivelseDto;
import no.fintlabs.model.instance.DokumentobjektDto;
import no.fintlabs.web.archive.FintArchiveClient;
import no.fintlabs.web.file.FileClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
public class FileDispatchService {

    private final FintArchiveClient fintArchiveClient;
    private final FileClient fileClient;

    public FileDispatchService(FintArchiveClient fintArchiveClient, FileClient fileClient) {
        this.fintArchiveClient = fintArchiveClient;
        this.fileClient = fileClient;
    }

    public Mono<FilesDispatchResult> dispatchFiles(Collection<DokumentbeskrivelseDto> dokumentbeskrivelseDtos) {
        if (dokumentbeskrivelseDtos.isEmpty()) {
            return Mono.just(FilesDispatchResult.accepted(Map.of()));
        }
        return Flux.fromStream(dokumentbeskrivelseDtos
                        .stream()
                        .map(DokumentbeskrivelseDto::getDokumentobjekt)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(Collection::stream))
                .concatMap(this::dispatchFile)
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
                .map(fileIds -> createFunctionalWarningMessage(fileIds, "id"))
                .onErrorResume(e -> {
                    log.error("Unable to get fileIds", e);
                    return Mono.just(createFunctionalWarningMessage(
                            fileLinks.stream()
                                    .map(Link::getHref)
                                    .toList(),
                            "link")
                    );
                });
    }

    private Optional<String> createFunctionalWarningMessage(List<String> fileRef, String refName) {
        if (fileRef.isEmpty()) {
            return Optional.empty();
        }
        if (fileRef.size() == 1) {
            return Optional.of("dokumentobjekt with " + refName + "='" + fileRef.get(0) + "'");
        }
        return Optional.of("dokumentobjekts with " + refName + "s=" + fileRef.stream().collect(joining("', '", "['", "']")));
    }

    private Mono<FileDispatchResult> dispatchFile(DokumentobjektDto dokumentobjektDto) {
        return dokumentobjektDto.getFileId().map(fileId ->
                fileClient.getFile(fileId)
                        .flatMap(file -> fintArchiveClient.postFile(file)
                                .map(link -> FileDispatchResult.accepted(fileId, link))
                                .onErrorResume(WebClientResponseException.class, e -> Mono.just(
                                        FileDispatchResult.declined(fileId, e.getResponseBodyAsString())
                                ))
                                .onErrorResume(e -> Mono.just(
                                        FileDispatchResult.failed(fileId)
                                ))
                        ).onErrorResume(e -> Mono.just(
                                FileDispatchResult.couldNotBeRetrieved(fileId)
                        ))
        ).orElse(Mono.just(FileDispatchResult.noFileId()));
    }

    private Mono<List<String>> getFileIds(Collection<Link> fileLinks) {
        return Flux.fromIterable(fileLinks)
                .concatMap(fintArchiveClient::getFile)
                .map(DokumentfilResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .collectList();
    }
}
