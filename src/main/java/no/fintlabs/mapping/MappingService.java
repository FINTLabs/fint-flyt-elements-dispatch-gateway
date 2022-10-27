package no.fintlabs.mapping;

import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.*;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.fintlabs.model.mappedinstance.Document;
import no.fintlabs.model.mappedinstance.MappedInstanceElement;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MappingService {

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

        toSkjermingResource(caseInstanceElement).ifPresent(sakResource::setSkjerming);
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

    public JournalpostResource toJournalpostResource(MappedInstanceElement recordInstanceElement) {
        JournalpostResource journalpostResource = new JournalpostResource();
        recordInstanceElement.getFieldValue("tittel").ifPresent(journalpostResource::setTittel);
        recordInstanceElement.getFieldValue("offentligTittel").ifPresent(journalpostResource::setOffentligTittel);

        // TODO: 25/10/2022 Dokumenttype?

        recordInstanceElement.getFieldValue("administrativenhet").map(Link::with).ifPresent(journalpostResource::addAdministrativEnhet);
        recordInstanceElement.getFieldValue("journalstatus").map(Link::with).ifPresent(journalpostResource::addJournalstatus);
        recordInstanceElement.getFieldValue("journalposttype").map(Link::with).ifPresent(journalpostResource::addJournalposttype);
        recordInstanceElement.getFieldValue("saksbehandler").map(Link::with).ifPresent(journalpostResource::addSaksbehandler);

        toSkjermingResource(recordInstanceElement).ifPresent(journalpostResource::setSkjerming);

        return journalpostResource;
    }

    public KorrespondansepartResource toKorrespondansepartResource(MappedInstanceElement applicantInstanceElement) {
        KorrespondansepartResource korrespondansepartResource = new KorrespondansepartResource();
        // TODO: 25/10/2022 type -- må ha link fra FINT?

        // TODO: 25/10/2022 Filtrer bort den som ikke skal settes?

        applicantInstanceElement.getFieldValue("organisasjonsnummer").ifPresent(korrespondansepartResource::setOrganisasjonsnummer);
        applicantInstanceElement.getFieldValue("fødselsnummer").ifPresent(korrespondansepartResource::setFodselsnummer);
        applicantInstanceElement.getFieldValue("KorrespondansepartNavn").ifPresent(korrespondansepartResource::setKorrespondansepartNavn);

        toAdresseResource(applicantInstanceElement).ifPresent(korrespondansepartResource::setAdresse);

        applicantInstanceElement.getFieldValue("kontaktperson").ifPresent(korrespondansepartResource::setKontaktperson);
        toKontaktinformasjon(applicantInstanceElement).ifPresent(korrespondansepartResource::setKontaktinformasjon);

        if (applicantInstanceElement.getFieldValue("protected").map(Boolean::parseBoolean).orElse(false)) {
            toSkjermingResource(applicantInstanceElement).ifPresent(korrespondansepartResource::setSkjerming);
        }

        return korrespondansepartResource;
    }

    private Optional<SkjermingResource> toSkjermingResource(MappedInstanceElement mappedInstanceElement) {
        Optional<String> tilgangsrestriksjon = mappedInstanceElement.getFieldValue("tilgangsrestriksjon");
        Optional<String> skjermingshjemmel = mappedInstanceElement.getFieldValue("skjermingshjemmel");
        if (tilgangsrestriksjon.isEmpty() && skjermingshjemmel.isEmpty()) {
            return Optional.empty();
        }
        SkjermingResource skjermingResource = new SkjermingResource();
        tilgangsrestriksjon.map(Link::with).ifPresent(skjermingResource::addTilgangsrestriksjon);
        skjermingshjemmel.map(Link::with).ifPresent(skjermingResource::addSkjermingshjemmel);
        return Optional.of(skjermingResource);
    }

    private Optional<AdresseResource> toAdresseResource(MappedInstanceElement applicantInstanceElement) {
        Optional<String> adresselinje = applicantInstanceElement.getFieldValue("Adresse.adresselinje");
        Optional<String> postnummer = applicantInstanceElement.getFieldValue("Adresse.postnummer");
        Optional<String> poststed = applicantInstanceElement.getFieldValue("Adresse.poststed");

        if (Stream.of(adresselinje, postnummer, poststed).allMatch(Optional::isEmpty)) {
            return Optional.empty();
        }
        AdresseResource adresseResource = new AdresseResource();
        adresselinje.map(List::of).ifPresent(adresseResource::setAdresselinje);
        postnummer.ifPresent(adresseResource::setPostnummer);
        poststed.ifPresent(adresseResource::setPoststed);
        return Optional.of(adresseResource);
    }

    private Optional<Kontaktinformasjon> toKontaktinformasjon(MappedInstanceElement applicantInstanceElement) {
        Optional<String> mobiltelefonnummer = applicantInstanceElement.getFieldValue("Kontaktinformasjon.mobiltelefonnummer");
        Optional<String> epostadresse = applicantInstanceElement.getFieldValue("Kontaktinformasjon.epostadresse");

        if (mobiltelefonnummer.isEmpty() && epostadresse.isEmpty()) {
            return Optional.empty();
        }
        Kontaktinformasjon kontaktinformasjon = new Kontaktinformasjon();
        mobiltelefonnummer.ifPresent(kontaktinformasjon::setMobiltelefonnummer);
        epostadresse.ifPresent(kontaktinformasjon::setEpostadresse);
        return Optional.of(kontaktinformasjon);
    }

    public List<DokumentbeskrivelseResource> toDokumentbeskrivelseResources(
            Collection<Document> documents,
            MappedInstanceElement documentInstanceElement
    ) {
        return documents
                .stream()
                .map(document -> toDokumentBeskrivelseResource(document, documentInstanceElement))
                .toList();
    }

    private DokumentbeskrivelseResource toDokumentBeskrivelseResource(
            Document document,
            MappedInstanceElement documentInstanceElement
    ) {
        DokumentbeskrivelseResource dokumentbeskrivelseResource = new DokumentbeskrivelseResource();
        dokumentbeskrivelseResource.setTittel(
                documentInstanceElement.getFieldValue("tittel").map(value -> value + "_").orElse("")
                        + document.getName()
        );
        documentInstanceElement.getFieldValue("dokumentStatus").map(Link::with).ifPresent(dokumentbeskrivelseResource::addDokumentstatus);

        DokumentobjektResource dokumentobjektResource = new DokumentobjektResource();
        documentInstanceElement.getFieldValue("DokumentBeskrivelse.dokumentObjekt.variantFormat")
                .map(Link::with)
                .ifPresent(dokumentobjektResource::addVariantFormat);

        // TODO: 25/10/2022 Add file
        dokumentbeskrivelseResource.setDokumentobjekt(List.of(dokumentobjektResource));
        return dokumentbeskrivelseResource;
    }

}
