package no.fintlabs.kafka;

import no.fintlabs.DispatchService;
import no.fintlabs.flyt.kafka.requestreply.InstanceFlowReplyProducerRecord;
import no.fintlabs.flyt.kafka.requestreply.InstanceFlowRequestConsumerFactoryService;
import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicService;
import no.fintlabs.model.Result;
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
            InstanceFlowRequestConsumerFactoryService instanceFlowRequestConsumerFactoryService,
            DispatchService dispatchService
    ) {
        RequestTopicNameParameters requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .resource("dispatch-instance")
                .build();

        requestTopicService.ensureTopic(requestTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        return instanceFlowRequestConsumerFactoryService.createFactory(
                MappedInstance.class,
                Result.class,
                instanceFlowConsumerRecord -> {
                    Result result = dispatchService.process(
                            instanceFlowConsumerRecord.getInstanceFlowHeaders(),
                            instanceFlowConsumerRecord.getConsumerRecord().value()
                    ).block();
                    return InstanceFlowReplyProducerRecord
                            .<Result>builder()
                            .instanceFlowHeaders(instanceFlowConsumerRecord.getInstanceFlowHeaders())
                            .value(result)
                            .build();
                },
                new CommonLoggingErrorHandler()
        ).createContainer(requestTopicNameParameters);

    }
}
