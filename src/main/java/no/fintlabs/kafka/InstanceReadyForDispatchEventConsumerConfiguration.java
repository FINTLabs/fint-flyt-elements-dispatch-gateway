package no.fintlabs.kafka;

import no.fintlabs.DispatchService;
import no.fintlabs.exceptions.InstanceDispatchDeclinedException;
import no.fintlabs.exceptions.InstanceDispatchFailedException;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventConsumerFactoryService;
import no.fintlabs.kafka.event.EventConsumerConfiguration;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.model.Result;
import no.fintlabs.model.instance.ArchiveInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
public class InstanceReadyForDispatchEventConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, ArchiveInstance> instanceReadyForDispatchEventConsumer(
            InstanceFlowEventConsumerFactoryService instanceFlowEventConsumerFactoryService,
            DispatchService dispatchService,
            InstanceDispatchedEventProducerService instanceDispatchedEventProducerService
    ) {
        return instanceFlowEventConsumerFactoryService.createRecordFactory(
                ArchiveInstance.class,
                instanceFlowConsumerRecord -> {
                    Result result = dispatchService.process(
                            instanceFlowConsumerRecord.getInstanceFlowHeaders(),
                            instanceFlowConsumerRecord.getConsumerRecord().value()
                    ).block();
                    if (result == null) {
                        throw new InstanceDispatchFailedException();
                    }
                    switch (result.getStatus()) {
                        case ACCEPTED -> instanceDispatchedEventProducerService.publish(
                                instanceFlowConsumerRecord.getInstanceFlowHeaders()
                                        .toBuilder()
                                        .archiveInstanceId(result.getArchiveCaseId())
                                        .build()
                        );
                        case DECLINED -> throw new InstanceDispatchDeclinedException(result.getErrorMessage());
                        case FAILED -> throw new InstanceDispatchFailedException();
                    }
                },
                EventConsumerConfiguration
                        .builder()
                        .maxPollIntervalMs(1800000)
                        .maxPollRecords(1)
                        .ackMode(ContainerProperties.AckMode.RECORD)
                        .build()
        ).createContainer(
                EventTopicNameParameters.builder()
                        .eventName("instance-ready-for-dispatch")
                        .build()
        );
    }

}
