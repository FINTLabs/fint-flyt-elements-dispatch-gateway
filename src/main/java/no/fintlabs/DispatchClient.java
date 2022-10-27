package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.kafka.TempKafkaDispatchProducerService;
import no.fintlabs.model.Result;
import no.fintlabs.model.Status;
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
public class DispatchClient {


    private final WebClient webClient;
    private final TempKafkaDispatchProducerService tempKafkaDispatchProducerService;

    public DispatchClient(WebClient webClient, TempKafkaDispatchProducerService tempKafkaDispatchProducerService) {
        this.webClient = webClient;
        this.tempKafkaDispatchProducerService = tempKafkaDispatchProducerService;
    }

    public Mono<Result> dispatchNewCase(SakResource sakResource) {
        tempKafkaDispatchProducerService.publish(sakResource);
        return postCase(sakResource)
                .delayElement(Duration.ofMillis(200))
                .flatMap(this::getCreatedRedirect)
                .flatMap(this::getCreatedCase)
                .map(resultSakResource -> new Result(Status.ACCEPTED, resultSakResource.getMappeId().getIdentifikatorverdi()))
                .doOnError(e -> {
                    if (e instanceof WebClientResponseException) {
                        log.error(e + " body=" + ((WebClientResponseException) e).getResponseBodyAsString());
                    } else {
                        log.error(e.toString());
                    }
                })
                .onErrorResume(WebClientResponseException.class, e ->
                        Mono.just(new Result(
                                e.getStatusCode().is4xxClientError()
                                        ? Status.DECLINED
                                        : Status.FAILED,
                                null
                        ))
                )
                .onErrorReturn(RuntimeException.class, new Result(Status.FAILED, null));
    }

    private Mono<URI> postCase(SakResource sakResource) {
        return webClient
                .post()
                .uri("/arkiv/noark/sak")
                .bodyValue(sakResource)
                .retrieve()
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

    private Mono<URI> getCreatedRedirect(URI uri) {
        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .toBodilessEntity()
                .filter(entity -> HttpStatus.CREATED.equals(entity.getStatusCode()) && entity.getHeaders().getLocation() != null)
                .mapNotNull(entity -> entity.getHeaders().getLocation())
                .repeatWhenEmpty(10, longFlux -> Flux.interval(Duration.ofSeconds(1)))
                .switchIfEmpty(Mono.error(new RuntimeException("Reached max number of retries for getting a 201 Created location header")));
    }

    private Mono<SakResource> getCreatedCase(URI uri) {
        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(SakResource.class);
    }

    public Mono<Result> dispatchToCollectionCase(String collectionCaseId, JournalpostResource journalpostResource) {
        log.info("Dispatching to collection case: collectionCaseId= '" + collectionCaseId + "' journalpostResource=" + journalpostResource.toString());
        return Mono.just(new Result(Status.FAILED, null));
    }

    public Mono<Result> dispatchToExistingOrAsNewCase(SakResource sakResource) {
        log.info("Dispatching to existing or as new case: sakResource=" + sakResource.toString());
        tempKafkaDispatchProducerService.publish(sakResource);
        return Mono.just(new Result(Status.FAILED, null));
    }

}
