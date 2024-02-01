package no.fintlabs.flyt.gateway.application.archive.kafka;

import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.kodeverk.SaksmappetypeResource;
import no.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource;
import no.fint.model.resource.arkiv.noark.AdministrativEnhetResource;
import no.fint.model.resource.arkiv.noark.ArkivdelResource;
import no.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.flyt.gateway.application.archive.links.ResourceLinkUtil;
import no.fintlabs.kafka.entity.EntityConsumerConfiguration;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Configuration
public class ResourceEntityConsumersConfiguration {

    private final EntityConsumerFactoryService entityConsumerFactoryService;


    public ResourceEntityConsumersConfiguration(EntityConsumerFactoryService entityConsumerFactoryService) {
        this.entityConsumerFactoryService = entityConsumerFactoryService;
    }

    private <T extends FintLinks> ConcurrentMessageListenerContainer<String, T> createCacheConsumer(
            String resourceReference,
            Class<T> resourceClass,
            FintCache<String, T> cache
    ) {
        return createCacheConsumer(
                resourceReference,
                resourceClass,
                cache,
                ResourceLinkUtil::getSelfLinks
        );
    }

    private <T> ConcurrentMessageListenerContainer<String, T> createCacheConsumer(
            String resourceReference,
            Class<T> resourceClass,
            FintCache<String, T> cache,
            Function<T, List<String>> cacheKeyFunction
    ) {
        return entityConsumerFactoryService.createBatchConsumerFactory(
                resourceClass,
                consumerRecords -> cache.put(
                        consumerRecords
                                .stream()
                                .map(ConsumerRecord::value)
                                .flatMap(value ->
                                        cacheKeyFunction
                                                .apply(value)
                                                .stream()
                                                .map(key -> new AbstractMap.SimpleEntry<>(key, value))
                                )
                                .collect(toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (a, b) -> b)
                                )
                ),
                EntityConsumerConfiguration
                        .builder()
                        .groupIdSuffix(resourceReference.replace('.', '-'))
                        .build()
        ).createContainer(EntityTopicNameParameters.builder().resource(resourceReference).build());
    }

    @Bean
    ConcurrentMessageListenerContainer<String, ArkivdelResource> arkivdelResourceEntityConsumer(
            FintCache<String, ArkivdelResource> arkivdelResourceCache
    ) {
        return createCacheConsumer(
                "arkiv.noark.arkivdel",
                ArkivdelResource.class,
                arkivdelResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, AdministrativEnhetResource> administrativEnhetResourceEntityConsumer(
            FintCache<String, AdministrativEnhetResource> administrativEnhetResourceCache
    ) {
        return createCacheConsumer(
                "arkiv.noark.administrativenhet",
                AdministrativEnhetResource.class,
                administrativEnhetResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceEntityConsumer(
            FintCache<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceCache
    ) {
        return createCacheConsumer(
                "arkiv.kodeverk.tilgangsrestriksjon",
                TilgangsrestriksjonResource.class,
                tilgangsrestriksjonResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, SaksmappetypeResource> saksmappetypeEntityConsumer(
            FintCache<String, SaksmappetypeResource> saksmappeTypeResourceCache
    ) {
        return createCacheConsumer(
                "arkiv.kodeverk.saksmappetype",
                SaksmappetypeResource.class,
                saksmappeTypeResourceCache
        );
    }

    @Bean
    ConcurrentMessageListenerContainer<String, KlassifikasjonssystemResource> klassifikasjonssystemResourceEntityConsumer(
            FintCache<String, KlassifikasjonssystemResource> klassifikasjonssystemResourceCache
    ) {
        return createCacheConsumer(
                "arkiv.noark.klassifikasjonssystem",
                KlassifikasjonssystemResource.class,
                klassifikasjonssystemResourceCache
        );
    }

}
