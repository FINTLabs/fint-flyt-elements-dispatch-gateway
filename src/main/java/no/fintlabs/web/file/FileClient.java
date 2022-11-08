package no.fintlabs.web.file;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.model.File;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class FileClient {

    private final WebClient fileWebClient;

    public FileClient(@Qualifier("fileWebClient") WebClient fileWebClient) {
        this.fileWebClient = fileWebClient;
    }

    public Mono<File> getFile(UUID fileId) {
        return fileWebClient
                .get()
                .uri("/" + fileId)
                .retrieve()
                .bodyToMono(File.class)
                .doOnEach(fileSignal -> log.info(fileSignal.toString()))
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(1)))
                .doOnError(
                        e -> log.error("Could not find file with id=" + fileId, e)
                );
    }

}
