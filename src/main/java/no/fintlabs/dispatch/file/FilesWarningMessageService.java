package no.fintlabs.dispatch.file;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentfilResource;
import no.fintlabs.dispatch.DispatchMessageFormattingService;
import no.fintlabs.web.archive.FintArchiveClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FilesWarningMessageService {

    private final DispatchMessageFormattingService dispatchMessageFormattingService;
    private final FintArchiveClient fintArchiveClient;

    public FilesWarningMessageService(
            DispatchMessageFormattingService dispatchMessageFormattingService,
            FintArchiveClient fintArchiveClient
    ) {
        this.dispatchMessageFormattingService = dispatchMessageFormattingService;
        this.fintArchiveClient = fintArchiveClient;
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
                .concatMap(this::getFileId)
                .collectList();
    }

    public Mono<String> getFileId(Link fileLink) {
        return fintArchiveClient.getFile(fileLink)
                .map(DokumentfilResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi);
    }

}
