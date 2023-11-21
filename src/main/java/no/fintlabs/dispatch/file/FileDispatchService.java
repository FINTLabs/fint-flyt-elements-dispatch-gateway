package no.fintlabs.dispatch.file;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentfilResource;
import no.fintlabs.dispatch.file.result.FileDispatchResult;
import no.fintlabs.model.instance.DokumentobjektDto;
import no.fintlabs.web.archive.FintArchiveClient;
import no.fintlabs.web.file.FileClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class FileDispatchService {

    private final FintArchiveClient fintArchiveClient;
    private final FileClient fileClient;

    public FileDispatchService(FintArchiveClient fintArchiveClient, FileClient fileClient) {
        this.fintArchiveClient = fintArchiveClient;
        this.fileClient = fileClient;
    }

    public Mono<FileDispatchResult> dispatch(DokumentobjektDto dokumentobjektDto) {
        log.info("Dispatching file");
        return dokumentobjektDto.getFileId().map(fileId -> fileClient.getFile(fileId)
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
                ).orElse(Mono.just(FileDispatchResult.noFileId()))
                .doOnNext(result -> log.info("Dispatch result=" + result.toString()));
    }

    public Mono<String> getFileId(Link fileLink) {
        return fintArchiveClient.getFile(fileLink)
                .map(DokumentfilResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi);
    }

}
