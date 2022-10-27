package no.fintlabs.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.model.mappedinstance.MappedInstanceElement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CaseMappingService {

    private final ShieldingMappingService shieldingMappingService;

    public CaseMappingService(ShieldingMappingService shieldingMappingService) {
        this.shieldingMappingService = shieldingMappingService;
    }

    public SakResource toSakResource(MappedInstanceElement caseInstanceElement) {
        SakResource sakResource = new SakResource();
        caseInstanceElement.getFieldValue("tittel").ifPresent(sakResource::setTittel);
        caseInstanceElement.getFieldValue("offentligTittel").ifPresent(sakResource::setOffentligTittel);
        caseInstanceElement.getFieldValue("saksmappetype").map(Link::with).ifPresent(sakResource::addSaksmappetype);
        caseInstanceElement.getFieldValue("administrativenhet").map(Link::with).ifPresent(sakResource::addAdministrativEnhet);
        caseInstanceElement.getFieldValue("arkivdel").map(Link::with).ifPresent(sakResource::addArkivdel);
        caseInstanceElement.getFieldValue("journalenhet").map(Link::with).ifPresent(sakResource::addJournalenhet);
        caseInstanceElement.getFieldValue("status").map(Link::with).ifPresent(sakResource::addSaksstatus);
        caseInstanceElement.getFieldValue("saksansvarlig").map(Link::with).ifPresent(sakResource::addSaksansvarlig);

        shieldingMappingService.toSkjermingResource(caseInstanceElement).ifPresent(sakResource::setSkjerming);
        sakResource.setKlasse(toKlasser(caseInstanceElement));

        return sakResource;
    }

    private List<KlasseResource> toKlasser(MappedInstanceElement caseInstanceField) {
        AtomicInteger order = new AtomicInteger();
        return Stream.of(
                        toKlasse(caseInstanceField, "primar")
                                .map(klasseResource -> {
                                    klasseResource.setRekkefolge(order.getAndIncrement());
                                    return klasseResource;
                                }),
                        toKlasse(caseInstanceField, "sekundar")
                                .map(klasseResource -> {
                                    klasseResource.setRekkefolge(order.getAndIncrement());
                                    return klasseResource;
                                }),
                        toKlasse(caseInstanceField, "tertiar")
                                .map(klasseResource -> {
                                    klasseResource.setRekkefolge(order.getAndIncrement());
                                    return klasseResource;
                                })
                ).filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<KlasseResource> toKlasse(MappedInstanceElement caseInstanceField, String fieldNamePrefix) {
        Optional<String> klasseId = caseInstanceField.getFieldValue(fieldNamePrefix + "klasse");
        Optional<String> tittel = caseInstanceField.getFieldValue(fieldNamePrefix + "tittel");
        Optional<String> ordningsprinsipp = caseInstanceField.getFieldValue(fieldNamePrefix + "ordningsprinsipp");
        if (Stream.of(klasseId, tittel, ordningsprinsipp).allMatch(Optional::isEmpty)) {
            return Optional.empty();
        }
        KlasseResource klasse = new KlasseResource();
        klasseId.ifPresent(klasse::setKlasseId);
        tittel.ifPresent(klasse::setTittel);
        ordningsprinsipp.map(Link::with).ifPresent(klasse::addKlassifikasjonssystem);
        return Optional.of(klasse);
    }

}
