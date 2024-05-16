package no.fintlabs.flyt.gateway.application.archive.resource.configuration;

import lombok.Builder;
import lombok.Getter;
import no.fintlabs.cache.FintCache;

import java.util.List;
import java.util.function.Function;

@Builder
@Getter
public class ResourcePipelineCacheProperties<T> {
    private Function<T, List<String>> createKeys;
    private FintCache<String, T> cache;
}
