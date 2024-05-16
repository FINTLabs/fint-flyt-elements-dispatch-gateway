package no.fintlabs.flyt.gateway.application.archive.resource.configuration;

import no.fint.model.resource.FintLinks;
import no.fintlabs.cache.FintCache;
import no.fintlabs.flyt.gateway.application.archive.links.ResourceLinkUtil;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FintLinksResourcePipelineFactory {
    public <T extends FintLinks> ResourcePipeline<T> createResourcePipeLine(
            Class<T> resourceClass,
            FintCache<String, T> cache,
            List<String> resourceReferencePath,
            boolean publishOnKafka
    ) {
        ResourcePipeline.ResourcePipelineBuilder<T> resourcePipelineBuilder = ResourcePipeline
                .<T>builder()
                .resourceClass(resourceClass)
                .urlResourcePath(String.join("/", resourceReferencePath))
                .cacheProperties(
                        ResourcePipelineCacheProperties
                                .<T>builder()
                                .createKeys(ResourceLinkUtil::getSelfLinks)
                                .cache(cache)
                                .build()
                );

        if (publishOnKafka) {
            resourcePipelineBuilder.kafkaProperties(
                    ResourcePipelineKafkaProperties
                            .<T>builder()
                            .createKafkaKey(ResourceLinkUtil::getFirstSelfLink)
                            .topicNameParameters(
                                    EntityTopicNameParameters
                                            .builder()
                                            .resource(String.join(".", resourceReferencePath))
                                            .build()
                            )
                            .build()
            );
        }

        return resourcePipelineBuilder.build();
    }
}
