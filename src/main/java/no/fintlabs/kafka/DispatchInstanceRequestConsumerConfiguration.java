package no.fintlabs.kafka;

import no.fintlabs.DispatchService;
import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.kafka.requestreply.RequestConsumerFactoryService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicService;
import no.fintlabs.model.mappedinstance.MappedInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class DispatchInstanceRequestConsumerConfiguration {

    @Bean
    ConcurrentMessageListenerContainer<String, MappedInstance> dispatchInstanceRequestConsumer(
            RequestTopicService requestTopicService,
            RequestConsumerFactoryService requestConsumerFactoryService,
            DispatchService dispatchService
    ) {
        RequestTopicNameParameters requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .resource("dispatch-instance")
                .build();

        requestTopicService.ensureTopic(requestTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        return requestConsumerFactoryService.createFactory(
                MappedInstance.class,
                Object.class,
                consumerRecord -> ReplyProducerRecord
                        .builder()
                        .value(dispatchService.dispatch(consumerRecord.value()))
                        .build(),
                new CommonLoggingErrorHandler()
        ).createContainer(requestTopicNameParameters);

    }
}
