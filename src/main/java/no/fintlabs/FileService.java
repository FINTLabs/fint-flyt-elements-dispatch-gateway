package no.fintlabs;

import no.fint.model.resource.Link;
import no.fintlabs.web.archive.FintArchiveClient;
import no.fintlabs.web.file.FileClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Service
public class FileService {

    private final FileClient fileClient;
    private final FintArchiveClient fintArchiveClient;

    public FileService(FileClient fileClient, FintArchiveClient fintArchiveClient) {
        this.fileClient = fileClient;
        this.fintArchiveClient = fintArchiveClient;
    }

    public Mono<Map<UUID, Link>> dispatchFiles(Collection<UUID> fileIds) {
        return Flux.fromIterable(fileIds)
                .flatMap(fileId -> Mono.zip(
                        Mono.just(fileId),
                        archiveFile(fileId)
                ))
                .collectMap(Tuple2::getT1, Tuple2::getT2);
    }

    private Mono<Link> archiveFile(UUID fileId) {
        return fileClient.getFile(fileId)
                .flatMap(fintArchiveClient::postFile)
                .map(URI::toString)
                .map(Link::with);
    }

}
