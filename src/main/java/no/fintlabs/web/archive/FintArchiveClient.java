package no.fintlabs.web.archive;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.DokumentfilResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.model.Result;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
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

    public Mono<URI> postFile(DokumentfilResource dokumentfilResource) {
        return pollForCreatedLocation(fintWebClient
                .post()
                .uri("/arkiv/noark/dokumentfil")
                .bodyValue(dokumentfilResource)
                .header("Content-Disposition", "attachment; filename='" + dokumentfilResource.getFilnavn() + "'")
                .retrieve()
        ).doOnError(e -> {
            if (e instanceof WebClientResponseException) {
                log.error(e + " body=" + ((WebClientResponseException) e).getResponseBodyAsString());
            } else {
                log.error(e.toString());
            }
        }).retryWhen(Retry.backoff(5, Duration.ofSeconds(1)));
    }

    public Mono<SakResource> getCase(String archiveCaseId) {
        return fintWebClient
                .get()
                .uri("/arkiv/noark/sak/mappeid/" + archiveCaseId)
                .retrieve()
                .bodyToMono(SakResource.class);
    }

    public Mono<Result> postCase(SakResource sakResource) {
        return pollForCaseResult(
                fintWebClient
                        .post()
                        .uri("/arkiv/noark/sak")
                        .bodyValue(sakResource)
                        .retrieve()
        );
    }

    public Mono<Result> putCase(String archiveCaseId, SakResource sakResource) {
        return pollForCaseResult(
                fintWebClient
                        .put()
                        .uri("/arkiv/noark/sak/" + archiveCaseId)
                        .bodyValue(sakResource)
                        .retrieve()
        );
    }

    private Mono<Result> pollForCaseResult(WebClient.ResponseSpec responseSpec) {
        return pollForCreatedLocation(responseSpec)
                .flatMap(this::getCase)
                .map(resultSakResource -> Result.accepted(resultSakResource.getMappeId().getIdentifikatorverdi()))
                .doOnError(e -> {
                    if (e instanceof WebClientResponseException) {
                        log.error(e + " body=" + ((WebClientResponseException) e).getResponseBodyAsString());
                    } else {
                        log.error(e.toString());
                    }
                })
                .onErrorResume(WebClientResponseException.class, e ->
                        Mono.just(Result.declined(e.getResponseBodyAsString()))
                )
                .onErrorReturn(RuntimeException.class, Result.failed());
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
                .repeatWhenEmpty(10, longFlux -> Flux.interval(Duration.ofSeconds(1)))
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
