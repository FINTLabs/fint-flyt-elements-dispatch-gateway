package no.fintlabs.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.kafka.event.EventProducer;
import no.fintlabs.kafka.event.EventProducerFactory;
import no.fintlabs.kafka.event.EventProducerRecord;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import org.springframework.stereotype.Service;

@Service
public class TempKafkaDispatchProducerService {

    EventProducer<SakResource> newCaseDataEventProducer;
    EventTopicNameParameters newCaseDataEventTopicNameParameters;

    EventProducer<CollectionCaseDataWrapper> collectionCaseDataEventProducer;
    EventTopicNameParameters collectionCaseDataEventTopicNameParameters;

    public TempKafkaDispatchProducerService(
            EventTopicService eventTopicService,
            EventProducerFactory eventProducerFactory
    ) {
        newCaseDataEventTopicNameParameters = EventTopicNameParameters
                .builder()
                .eventName("temp-new-case-data")
                .build();
        eventTopicService.ensureTopic(newCaseDataEventTopicNameParameters, 0);
        newCaseDataEventProducer = eventProducerFactory.createProducer(SakResource.class);

        collectionCaseDataEventTopicNameParameters = EventTopicNameParameters
                .builder()
                .eventName("temp-collection-case-data")
                .build();
        eventTopicService.ensureTopic(collectionCaseDataEventTopicNameParameters, 0);
        collectionCaseDataEventProducer = eventProducerFactory.createProducer(CollectionCaseDataWrapper.class);
    }

    public void publishNewCaseData(SakResource sakResource) {
        newCaseDataEventProducer.send(
                EventProducerRecord
                        .<SakResource>builder()
                        .topicNameParameters(newCaseDataEventTopicNameParameters)
                        .value(sakResource)
                        .build()
        );
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class CollectionCaseDataWrapper {
        private String archiveCollectionCaseId;
        private JournalpostResource journalpostResource;
    }

    public void publishCollectionCaseData(String archiveCollectionCaseId, JournalpostResource journalpostResource) {
        collectionCaseDataEventProducer.send(
                EventProducerRecord
                        .<CollectionCaseDataWrapper>builder()
                        .topicNameParameters(collectionCaseDataEventTopicNameParameters)
                        .value(new CollectionCaseDataWrapper(archiveCollectionCaseId, journalpostResource))
                        .build()
        );
    }
}
