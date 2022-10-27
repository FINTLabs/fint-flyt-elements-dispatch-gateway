package no.fintlabs.kafka;

import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.kafka.event.EventProducer;
import no.fintlabs.kafka.event.EventProducerFactory;
import no.fintlabs.kafka.event.EventProducerRecord;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import org.springframework.stereotype.Service;

@Service
public class TempKafkaDispatchProducerService {

    EventProducer<SakResource> newSakResourceEventProducer;
    EventTopicNameParameters topicNameParameters;

    public TempKafkaDispatchProducerService(
            EventTopicService eventTopicService,
            EventProducerFactory eventProducerFactory
    ) {
        topicNameParameters = EventTopicNameParameters
                .builder()
                .eventName("temp-dispatch-new-case-to-elements")
                .build();

        eventTopicService.ensureTopic(topicNameParameters, 0);

        newSakResourceEventProducer = eventProducerFactory.createProducer(SakResource.class);
    }

    public void publish(SakResource sakResource) {
        newSakResourceEventProducer.send(
                EventProducerRecord
                        .<SakResource>builder()
                        .topicNameParameters(topicNameParameters)
                        .value(sakResource)
                        .build()
        );
    }
}
