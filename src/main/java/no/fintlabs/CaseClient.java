package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.model.Result;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@Slf4j
@Service
public class CaseClient {

    private final WebClient webClient;

    public CaseClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<SakResource> getCase(String archiveCaseId) {
        return webClient
                .get()
                .uri("/arkiv/noark/sak/" + archiveCaseId)
                .retrieve()
                .bodyToMono(SakResource.class);
    }

    public Mono<Result> postCase(SakResource sakResource) {
        return pollForResult(
                webClient
                        .post()
                        .uri("/arkiv/noark/sak")
                        .bodyValue(sakResource)
                        .retrieve()
        );
    }

    public Mono<Result> putCase(String archiveCaseId, SakResource sakResource) {
        return pollForResult(
                webClient
                        .put()
                        .uri("/arkiv/noark/sak/" + archiveCaseId)
                        .bodyValue(sakResource)
                        .retrieve()
        );
    }

    private Mono<Result> pollForResult(WebClient.ResponseSpec responseSpec) {
        return responseSpec
                .toBodilessEntity()
                .<URI>handle((entity, sink) -> {
                            if (HttpStatus.ACCEPTED.equals(entity.getStatusCode())
                                    && entity.getHeaders().getLocation() != null) {
                                sink.next(entity.getHeaders().getLocation());
                            } else {
                                sink.error(new RuntimeException("Expected 202 Accepted response with redirect header"));
                            }
                        }
                )
                .delayElement(Duration.ofMillis(200))
                .flatMap(this::pollForCreatedLocation)
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

    private Mono<URI> pollForCreatedLocation(URI uri) {
        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .toBodilessEntity()
                .filter(entity -> HttpStatus.CREATED.equals(entity.getStatusCode()) && entity.getHeaders().getLocation() != null)
                .mapNotNull(entity -> entity.getHeaders().getLocation())
                .repeatWhenEmpty(10, longFlux -> Flux.interval(Duration.ofSeconds(1)))
                .switchIfEmpty(Mono.error(new RuntimeException("Reached max number of retries for polling for 201 Created location header")));
    }

    private Mono<SakResource> getCase(URI uri) {
        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(SakResource.class);
    }

}
