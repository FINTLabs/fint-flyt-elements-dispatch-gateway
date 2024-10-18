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

import java.util.Objects;

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
                                .doOnError(e -> log.error("Error before handling: {}", e.getMessage(), e))
                                .onErrorResume(IllegalStateException.class, e -> handleDispatchError(instanceFlowConsumerRecord, e, "IllegalStateException encountered during dispatch", instanceDispatchingErrorProducerService))
                                .onErrorResume(IllegalArgumentException.class, e -> handleDispatchError(instanceFlowConsumerRecord, e, "IllegalArgumentException encountered during dispatch", instanceDispatchingErrorProducerService))
                                .onErrorResume(NullPointerException.class, e -> handleDispatchError(instanceFlowConsumerRecord, e, "NullPointerException encountered during dispatch", instanceDispatchingErrorProducerService))
                                .onErrorResume(Throwable.class, e -> handleDispatchError(instanceFlowConsumerRecord, e, "Unexpected exception encountered during dispatch", instanceDispatchingErrorProducerService))
                                .retry(1)
                                .subscribe(
                                        null,
                                        error -> log.error("Unhandled error in subscriber: {}", error.getMessage(), error)
                                ),
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
        String errorMessage = (e != null && e.getMessage() != null) ? e.getMessage() : "Unknown error occurred";

        log.error("{}: {}", logMessage, errorMessage, e);

        if (instanceFlowConsumerRecord != null && instanceFlowConsumerRecord.getInstanceFlowHeaders() != null) {
            try {
                instanceDispatchingErrorProducerService.publishGeneralSystemErrorEvent(
                        instanceFlowConsumerRecord.getInstanceFlowHeaders(),
                        "An error occurred during dispatch: " + errorMessage
                );
            } catch (Exception ex) {
                log.error("Failed to publish general system error event to Kafka: {}", ex.getMessage(), ex);
            }
        } else {
            log.error("Cannot publish error event because InstanceFlowHeaders is null");
        }

        return Mono.error(Objects.requireNonNullElseGet(e, () -> new IllegalStateException("An unknown error occurred during dispatch")));
    }



}
