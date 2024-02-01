package no.fintlabs.flyt.gateway.application.archive.resource.web;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentfilResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fint.model.resource.arkiv.noark.SakResources;
import no.fintlabs.flyt.gateway.application.archive.resource.model.ObjectResources;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FintArchiveResourceClient {
    private final WebClient fintWebClient;

    private final Map<String, Long> sinceTimestamp = new ConcurrentHashMap<>();

    public FintArchiveResourceClient(WebClient fintWebClient) {
        this.fintWebClient = fintWebClient;
    }

    public Mono<List<Object>> getResourcesLastUpdated(String endpoint) {
        return getLastUpdated(endpoint)
                .flatMapIterable(ObjectResources::getContent)
                .collect(Collectors.toList());
    }

    public void resetLastUpdatedTimestamps() {
        this.sinceTimestamp.clear();
    }

    public Mono<DokumentfilResource> getFile(Link link) {
        return fintWebClient
                .get()
                .uri(URI.create(link.getHref()))
                .retrieve()
                .bodyToMono(DokumentfilResource.class);
    }

    private MediaType getMediaType(String mediaType) {
        try {
            return MediaType.parseMediaType(mediaType);
        } catch (InvalidMediaTypeException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    public Mono<List<SakResource>> findCasesWithFilter(String caseFilter) {
        return fintWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/arkiv/noark/sak")
                        .queryParam("$filter", caseFilter)
                        .build()
                )
                .retrieve()
                .bodyToMono(SakResources.class)
                .map(SakResources::getContent)
                .onErrorReturn(WebClientResponseException.NotFound.class, List.of())
                .doOnError(e -> {
                    if (e instanceof WebClientResponseException) {
                        log.error(e + " body=" + ((WebClientResponseException) e).getResponseBodyAsString());
                    } else {
                        log.error(e.toString());
                    }
                });
    }

    private Mono<ObjectResources> getLastUpdated(String endpoint) {
        return fintWebClient.get()
                .uri(endpoint.concat("/last-updated"))
                .retrieve()
                .bodyToMono(LastUpdated.class)
                .flatMap(lastUpdated -> fintWebClient.get()
                        .uri(endpoint, uriBuilder -> uriBuilder.queryParam("sinceTimeStamp", sinceTimestamp.getOrDefault(endpoint, 0L)).build())
                        .retrieve()
                        .bodyToMono(ObjectResources.class)
                        .doOnNext(it -> sinceTimestamp.put(endpoint, lastUpdated.getLastUpdated()))
                );
    }

    public Mono<Object> getResource(String endpoint) {
        return fintWebClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(Object.class);
    }

    public <T> Mono<T> getResource(String endpoint, Class<T> clazz) {
        return fintWebClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(clazz);
    }

    @Data
    private static class LastUpdated {
        private Long lastUpdated;
    }

    public void reset() {
        sinceTimestamp.clear();
    }

    public Mono<SakResource> getCase(URI uri) {
        return fintWebClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(SakResource.class);
    }
}