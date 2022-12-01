package no.fintlabs.web.archive;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentfilResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.kafka.TempKafkaDispatchProducerService;
import no.fintlabs.model.File;
import no.fintlabs.model.Result;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Service
public class FintArchiveService {

    private final FintArchiveClient fintArchiveClient;
    // TODO: 27/10/2022 Remove
    private final TempKafkaDispatchProducerService tempKafkaDispatchProducerService;

    public FintArchiveService(FintArchiveClient fintArchiveClient, TempKafkaDispatchProducerService tempKafkaDispatchProducerService) {
        this.fintArchiveClient = fintArchiveClient;
        this.tempKafkaDispatchProducerService = tempKafkaDispatchProducerService;
    }

    public Mono<Link> dispatchFile(File file) {
        return fintArchiveClient.postFile(file)
                .map(URI::toString)
                .map(Link::with);
    }

    public Mono<Result> dispatchNewCase(SakResource sakResource) {
        tempKafkaDispatchProducerService.publishNewCaseData(sakResource);
        return fintArchiveClient.postCase(sakResource);
    }

    public Mono<Result> dispatchToCollectionCase(String collectionCaseId, JournalpostResource journalpostResource) {
        tempKafkaDispatchProducerService.publishCollectionCaseData(collectionCaseId, journalpostResource);
        return fintArchiveClient.getCase(collectionCaseId)
                .map(sakResource -> {
                    sakResource.getJournalpost().add(journalpostResource);
                    return sakResource;
                })
                .flatMap(sakResource -> fintArchiveClient.putCase(collectionCaseId, sakResource));
    }

    public Mono<Result> dispatchToExistingOrAsNewCase(SakResource sakResource) {
        log.info("Dispatching to existing or as new case: sakResource=" + sakResource.toString());
        return Mono.just(Result.failed());
    }

}
