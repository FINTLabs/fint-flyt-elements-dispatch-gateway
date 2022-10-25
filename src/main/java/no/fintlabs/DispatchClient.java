package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.kafka.TempKafkaDispatchProducerService;
import no.fintlabs.model.Status;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DispatchClient {

    private final TempKafkaDispatchProducerService tempKafkaDispatchProducerService;

    public DispatchClient(TempKafkaDispatchProducerService tempKafkaDispatchProducerService) {
        this.tempKafkaDispatchProducerService = tempKafkaDispatchProducerService;
    }

    public Status dispatchNewCase(SakResource sakResource) {
        log.info("Dispatching sakResource=" + sakResource.toString());
        tempKafkaDispatchProducerService.publish(sakResource);
        return Status.ACCEPTED;
    }

    public Status dispatchToCollectionCase(String collectionCaseId, JournalpostResource journalpostResource) {
        log.info("Dispatching collectionCaseId= '" + collectionCaseId + "' journalpostResource=" + journalpostResource.toString());
        return Status.DECLINED;
    }
}
