package no.fintlabs.dispatch.file;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentfilResource;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
            return Mono.just(FilesDispatchResult.accepted(Collections.emptyList()));
        }
        return Flux.fromStream(dokumentbeskrivelseDtos
                        .stream()
                        .map(DokumentbeskrivelseDto::getDokumentobjekt)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(Collection::stream))
                .concatMap(this::dispatchFile)
                .takeWhile(fileDispatchResult -> fileDispatchResult.getStatus() == FileDispatchResult.Status.ACCEPTED)
                .collectList()
                .map(fileDispatchResults -> {

                    FileDispatchResult lastResult = fileDispatchResults.get(fileDispatchResults.size() - 1);
                    FileDispatchResult.Status lastStatus = lastResult.getStatus();
                    List<FileDispatchResult> successfulFileDispatches = lastStatus == FileDispatchResult.Status.ACCEPTED
                            ? fileDispatchResults
                            : fileDispatchResults.subList(0, fileDispatchResults.size() - 1);

                    return switch (lastStatus) {
                        case ACCEPTED -> FilesDispatchResult.accepted(
                                successfulFileDispatches
                        );
                        case DECLINED -> FilesDispatchResult.declined(
                                successfulFileDispatches,
                                lastResult
                        );
                        case COULD_NOT_BE_RETRIEVED, FAILED -> FilesDispatchResult.failed(
                                successfulFileDispatches,
                                lastResult
                        );
                    };
                });
    }

    public Mono<FileDispatchResult> dispatchFile(DokumentobjektDto dokumentobjektDto) {
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

}
