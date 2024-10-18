package no.fintlabs.flyt.gateway.application.archive.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.gateway.application.archive.dispatch.DispatchResult;
import no.fintlabs.flyt.gateway.application.archive.dispatch.DispatchService;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.ArchiveInstance;
import no.fintlabs.flyt.gateway.application.archive.kafka.error.InstanceDispatchingErrorProducerService;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventConsumerFactoryService;
import no.fintlabs.kafka.event.EventConsumerConfiguration;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class InstanceReadyForDispatchEventConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, ArchiveInstance> instanceReadyForDispatchEventConsumer(
            InstanceFlowEventConsumerFactoryService instanceFlowEventConsumerFactoryService,
            DispatchService dispatchService,
            InstanceDispatchedEventProducerService instanceDispatchedEventProducerService,
            InstanceDispatchingErrorProducerService instanceDispatchingErrorProducerService
    ) {
        return instanceFlowEventConsumerFactoryService.createRecordFactory(
                ArchiveInstance.class,
                instanceFlowConsumerRecord ->
                        dispatchService.process(
                                        instanceFlowConsumerRecord.getInstanceFlowHeaders(),
                                        instanceFlowConsumerRecord.getConsumerRecord().value()
                                ).doOnNext(dispatchResult -> {
                                    switch (dispatchResult.getStatus()) {
                                        case ACCEPTED -> instanceDispatchedEventProducerService.publish(
                                                instanceFlowConsumerRecord.getInstanceFlowHeaders()
                                                        .toBuilder()
                                                        .archiveInstanceId(dispatchResult.getArchiveCaseAndRecordsIds())
                                                        .build()
                                        );
                                        case DECLINED ->
                                                instanceDispatchingErrorProducerService.publishInstanceDispatchDeclinedErrorEvent(
                                                        instanceFlowConsumerRecord.getInstanceFlowHeaders(),
                                                        dispatchResult.getErrorMessage()
                                                );
                                        case FAILED -> instanceDispatchingErrorProducerService.publishGeneralSystemErrorEvent(
                                                instanceFlowConsumerRecord.getInstanceFlowHeaders(),
                                                dispatchResult.getErrorMessage()
                                        );
                                    }
                                })
                                .onErrorResume(IllegalStateException.class, e -> handleDispatchError(instanceFlowConsumerRecord, e, "IllegalStateException encountered during dispatch", instanceDispatchingErrorProducerService))
                                .onErrorResume(IllegalArgumentException.class, e -> handleDispatchError(instanceFlowConsumerRecord, e, "IllegalArgumentException encountered during dispatch", instanceDispatchingErrorProducerService))
                                .onErrorResume(NullPointerException.class, e -> handleDispatchError(instanceFlowConsumerRecord, e, "NullPointerException encountered during dispatch", instanceDispatchingErrorProducerService))
                                .onErrorResume(Throwable.class, e -> handleDispatchError(instanceFlowConsumerRecord, e, "Unexpected exception encountered during dispatch", instanceDispatchingErrorProducerService))
                                .subscribe(),
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

    private Mono<DispatchResult> handleDispatchError(
            InstanceFlowConsumerRecord<ArchiveInstance> instanceFlowConsumerRecord,
            Throwable e,
            String logMessage,
            InstanceDispatchingErrorProducerService instanceDispatchingErrorProducerService
    ) {
        instanceDispatchingErrorProducerService.publishGeneralSystemErrorEvent(
                instanceFlowConsumerRecord.getInstanceFlowHeaders(),
                "An error occurred during dispatch: " + e.getMessage()
        );
        log.error("{}: {}", logMessage, e.getMessage(), e);
        return Mono.just(DispatchResult.failed("An error occurred during dispatch: " + e.getMessage()));
    }

}
