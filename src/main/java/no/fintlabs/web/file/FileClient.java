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
        log.info("Getting file");
        return fileWebClient
                .get()
                .uri("/" + fileId)
                .retrieve()
                .bodyToMono(File.class)
                .retryWhen(Retry
                        .backoff(5, Duration.ofSeconds(1))
                        .doBeforeRetry(retrySignal -> log.warn(
                                "Could not retrieve file with id=" + fileId +
                                        " -- performing retry " + (retrySignal.totalRetries() + 1),
                                retrySignal.failure())
                        )
                )
                .doOnNext(file -> log.info("Retrieved file with id=" + fileId))
                .doOnError(e -> log.error("Could not retrieve file with id=" + fileId, e));
    }

}
