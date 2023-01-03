package no.fintlabs.web.archive;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.model.File;
import no.fintlabs.model.JournalpostWrapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;

@Slf4j
@Service
public class FintArchiveClient {

    private final WebClient fintWebClient;

    public FintArchiveClient(@Qualifier("fintWebClient") WebClient fintWebClient) {
        this.fintWebClient = fintWebClient;
    }

    public Mono<URI> postFile(File file) {
        return pollForCreatedLocation(fintWebClient
                .post()
                .uri("/arkiv/noark/dokumentfil")
                .contentType(getMediaType(file.getType()))
                .bodyValue(file.getContents())
                .header("Content-Disposition", "attachment; filename='" + file.getName() + "'")
                .retrieve()
        ).doOnError(e -> {
            if (e instanceof WebClientResponseException) {
                log.error(e + " body=" + ((WebClientResponseException) e).getResponseBodyAsString());
            } else {
                log.error(e.toString());
            }
        }).retryWhen(Retry.backoff(5, Duration.ofSeconds(1)));
    }

    private MediaType getMediaType(String mediaType) {
        try {
            return MediaType.parseMediaType("application/" + mediaType);
        } catch (InvalidMediaTypeException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    public Mono<SakResource> getCase(String archiveCaseId) {
        return fintWebClient
                .get()
                .uri("/arkiv/noark/sak/mappeid/" + archiveCaseId)
                .retrieve()
                .bodyToMono(SakResource.class);
    }

    public Mono<SakResource> postCase(SakResource sakResource) {
        return pollForCaseResult(
                fintWebClient
                        .post()
                        .uri("/arkiv/noark/sak")
                        .bodyValue(sakResource)
                        .retrieve()
        );
    }

    public Mono<SakResource> putRecord(String caseId, JournalpostWrapper journalpostWrapper) {
        return pollForCaseResult(
                fintWebClient
                        .put()
                        .uri("/arkiv/noark/sak/mappeid/" + caseId)
                        .bodyValue(journalpostWrapper)
                        .retrieve()
        );
    }

    private Mono<SakResource> pollForCaseResult(WebClient.ResponseSpec responseSpec) {
        return pollForCreatedLocation(responseSpec)
                .flatMap(this::getCase)
                .doOnError(e -> {
                    if (e instanceof WebClientResponseException) {
                        log.error(e + " body=" + ((WebClientResponseException) e).getResponseBodyAsString());
                    } else {
                        log.error(e.toString());
                    }
                });

    }

    private Mono<URI> pollForCreatedLocation(WebClient.ResponseSpec responseSpec) {
        return getStatusLocation(responseSpec)
                .delayElement(Duration.ofMillis(200))
                .flatMap(this::pollForCreatedLocation);
    }

    private Mono<URI> getStatusLocation(WebClient.ResponseSpec responseSpec) {
        return responseSpec
                .toBodilessEntity()
                .handle((entity, sink) -> {
                            if (HttpStatus.ACCEPTED.equals(entity.getStatusCode())
                                    && entity.getHeaders().getLocation() != null) {
                                sink.next(entity.getHeaders().getLocation());
                            } else {
                                sink.error(new RuntimeException("Expected 202 Accepted response with redirect header"));
                            }
                        }
                );
    }

    private Mono<URI> pollForCreatedLocation(URI statusUri) {
        return fintWebClient
                .get()
                .uri(statusUri)
                .retrieve()
                .toBodilessEntity()
                .filter(entity -> HttpStatus.CREATED.equals(entity.getStatusCode()) && entity.getHeaders().getLocation() != null)
                .mapNotNull(entity -> entity.getHeaders().getLocation())
                .repeatWhenEmpty(10, longFlux -> Flux.interval(Duration.ofSeconds(10)))
                .switchIfEmpty(Mono.error(new RuntimeException("Reached max number of retries for polling for 201 Created location header")));
    }

    private Mono<SakResource> getCase(URI uri) {
        return fintWebClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(SakResource.class);
    }

}
