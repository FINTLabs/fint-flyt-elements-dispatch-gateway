package no.fintlabs.flyt.gateway.application.archive.dispatch.file;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.gateway.application.archive.dispatch.file.result.FileDispatchResult;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.DokumentobjektDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.web.FintArchiveDispatchClient;
import no.fintlabs.flyt.gateway.application.archive.dispatch.web.file.FileClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class FileDispatchService {

    private final FintArchiveDispatchClient fintArchiveDispatchClient;
    private final FileClient fileClient;

    public FileDispatchService(FintArchiveDispatchClient fintArchiveDispatchClient, FileClient fileClient) {
        this.fintArchiveDispatchClient = fintArchiveDispatchClient;
        this.fileClient = fileClient;
    }

    public Mono<FileDispatchResult> dispatch(DokumentobjektDto dokumentobjektDto) {
        log.info("Dispatching file");
        return dokumentobjektDto.getFileId().map(fileId -> fileClient.getFile(fileId)
                        .flatMap(file -> fintArchiveDispatchClient.postFile(file)
                                .map(link -> FileDispatchResult.accepted(fileId, link))
                                .onErrorResume(WebClientResponseException.class, e -> Mono.just(
                                        FileDispatchResult.declined(fileId, e.getResponseBodyAsString())
                                ))
                                .onErrorResume(ReadTimeoutException.class, e -> {
                                    log.error("File dispatch timed out");
                                    return Mono.just(FileDispatchResult.timedOut(fileId));
                                })
                                .onErrorResume(e -> {
                                    log.error("File dispatch failed");
                                    return Mono.just(FileDispatchResult.failed(fileId));
                                })
                        ).onErrorResume(e -> {
                            log.error("File could not be retrieved");
                            return Mono.just(
                                    FileDispatchResult.couldNotBeRetrieved(fileId)
                            );
                        })
                ).orElse(Mono.just(FileDispatchResult.noFileId()))
                .doOnNext(result -> log.info("Dispatch result=" + result.toString()));
    }

}
