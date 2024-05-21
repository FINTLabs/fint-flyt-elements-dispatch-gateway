package no.fintlabs.flyt.gateway.application.archive.resource.configuration;

import lombok.Builder;
import lombok.Getter;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;

import java.util.function.Function;

@Builder
@Getter
public class ResourcePipelineKafkaProperties<T> {
    private Function<T, String> createKafkaKey;
    private EntityTopicNameParameters topicNameParameters;
}
