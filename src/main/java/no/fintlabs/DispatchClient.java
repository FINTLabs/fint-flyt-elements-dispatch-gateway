package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.kafka.TempKafkaDispatchProducerService;
import no.fintlabs.model.Result;
import no.fintlabs.model.Status;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class DispatchClient {

    private final CaseClient caseClient;
    // TODO: 27/10/2022 Remove
    private final TempKafkaDispatchProducerService tempKafkaDispatchProducerService;

    public DispatchClient(CaseClient caseClient, TempKafkaDispatchProducerService tempKafkaDispatchProducerService) {
        this.caseClient = caseClient;
        this.tempKafkaDispatchProducerService = tempKafkaDispatchProducerService;
    }

    public Mono<Result> dispatchNewCase(SakResource sakResource) {
        tempKafkaDispatchProducerService.publishNewCaseData(sakResource);
        return caseClient.postCase(sakResource);
    }

    public Mono<Result> dispatchToCollectionCase(String collectionCaseId, JournalpostResource journalpostResource) {
        tempKafkaDispatchProducerService.publishCollectionCaseData(collectionCaseId, journalpostResource);
        return caseClient.getCase(collectionCaseId)
                .map(sakResource -> {
                    sakResource.getJournalpost().add(journalpostResource);
                    return sakResource;
                })
                .flatMap(sakResource -> caseClient.putCase(collectionCaseId, sakResource));
    }

    public Mono<Result> dispatchToExistingOrAsNewCase(SakResource sakResource) {
        log.info("Dispatching to existing or as new case: sakResource=" + sakResource.toString());
        return Mono.just(new Result(Status.FAILED, null));
    }

}
