package no.fintlabs;

import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.*;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.fintlabs.model.mappedinstance.Document;
import no.fintlabs.model.mappedinstance.MappedInstanceElement;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class MappingService {

    public SakResource toSakResource(MappedInstanceElement caseInstanceField) {
        SakResource sakResource = new SakResource();
        sakResource.setTittel(caseInstanceField.getFieldValue("tittel"));
        sakResource.setOffentligTittel(caseInstanceField.getFieldValue("offentligTittel"));
        sakResource.addSaksmappetype(Link.with(caseInstanceField.getFieldValue("saksmappetype")));
        sakResource.addAdministrativEnhet(Link.with(caseInstanceField.getFieldValue("administrativenhet")));
        sakResource.addArkivdel(Link.with(caseInstanceField.getFieldValue("arkivdel")));
        sakResource.addJournalenhet(Link.with(caseInstanceField.getFieldValue("journalenhet")));
        sakResource.addSaksstatus(Link.with(caseInstanceField.getFieldValue("status")));

        SkjermingResource skjermingResource = new SkjermingResource();
        skjermingResource.addTilgangsrestriksjon(Link.with(caseInstanceField.getFieldValue("tilgangsrestriksjon")));
        skjermingResource.addSkjermingshjemmel(Link.with(caseInstanceField.getFieldValue("skjermingshjemmel")));
        sakResource.setSkjerming(skjermingResource);

        sakResource.addSaksansvarlig(Link.with(caseInstanceField.getFieldValue("saksansvarlig")));

        KlasseResource primarKlasse = new KlasseResource();
        primarKlasse.setRekkefolge(0);
        primarKlasse.setKlasseId(caseInstanceField.getFieldValue("primarklasse"));
        primarKlasse.setTittel(caseInstanceField.getFieldValue("primartittel"));
        primarKlasse.addKlassifikasjonssystem(Link.with(caseInstanceField.getFieldValue("primarordningsprinsipp")));

        KlasseResource sekundarKlasse = new KlasseResource();
        sekundarKlasse.setRekkefolge(1);
        sekundarKlasse.setKlasseId(caseInstanceField.getFieldValue("sekundarklasse"));
        sekundarKlasse.setTittel(caseInstanceField.getFieldValue("sekundartittel"));
        sekundarKlasse.addKlassifikasjonssystem(Link.with(caseInstanceField.getFieldValue("sekundarordningsprinsipp")));

        KlasseResource tertiarKlasse = new KlasseResource();
        tertiarKlasse.setRekkefolge(2);
        tertiarKlasse.setKlasseId(caseInstanceField.getFieldValue("tertiarklasse"));
        tertiarKlasse.setTittel(caseInstanceField.getFieldValue("tertiartittel"));
        tertiarKlasse.addKlassifikasjonssystem(Link.with(caseInstanceField.getFieldValue("tertiarordningsprinsipp")));

        sakResource.setKlasse(List.of(primarKlasse, sekundarKlasse, tertiarKlasse));

        return sakResource;
    }

    public JournalpostResource toJournalpostResource(MappedInstanceElement recordInstanceElement) {
        JournalpostResource journalpostResource = new JournalpostResource();
        journalpostResource.setTittel(recordInstanceElement.getFieldValue("tittel"));
        journalpostResource.setOffentligTittel(recordInstanceElement.getFieldValue("offentligTittel"));
        // TODO: 25/10/2022 Dokumenttype?
        journalpostResource.addAdministrativEnhet(Link.with(recordInstanceElement.getFieldValue("administrativenhet")));
        journalpostResource.addJournalstatus(Link.with(recordInstanceElement.getFieldValue("journalstatus")));
        journalpostResource.addJournalposttype(Link.with(recordInstanceElement.getFieldValue("journalposttype")));
        journalpostResource.addSaksbehandler(Link.with(recordInstanceElement.getFieldValue("saksbehandler")));

        SkjermingResource skjermingResource = new SkjermingResource();
        skjermingResource.addTilgangsrestriksjon(Link.with(recordInstanceElement.getFieldValue("tilgangsrestriksjon")));
        skjermingResource.addSkjermingshjemmel(Link.with(recordInstanceElement.getFieldValue("skjermingshjemmel")));
        journalpostResource.setSkjerming(skjermingResource);

        return journalpostResource;
    }

    public KorrespondansepartResource toKorrespondansepartResource(MappedInstanceElement applicantInstanceElement) {
        KorrespondansepartResource korrespondansepartResource = new KorrespondansepartResource();
        // TODO: 25/10/2022 type -- må ha link fra FINT?

        // TODO: 25/10/2022 Filtrer bort den som ikke skal settes?
        korrespondansepartResource.setOrganisasjonsnummer(applicantInstanceElement.getFieldValue("organisasjonsnummer"));
        korrespondansepartResource.setFodselsnummer(applicantInstanceElement.getFieldValue("fødselsnummer"));

        korrespondansepartResource.setKorrespondansepartNavn(applicantInstanceElement.getFieldValue("KorrespondansepartNavn"));

        AdresseResource adresseResource = new AdresseResource();
        adresseResource.setAdresselinje(List.of(applicantInstanceElement.getFieldValue("Adresse.adresselinje")));
        adresseResource.setPostnummer(applicantInstanceElement.getFieldValue("Adresse.postnummer"));
        adresseResource.setPoststed(applicantInstanceElement.getFieldValue("Adresse.poststed"));
        korrespondansepartResource.setAdresse(adresseResource);

        korrespondansepartResource.setKontaktperson(applicantInstanceElement.getFieldValue("kontaktperson"));

        Kontaktinformasjon kontaktinformasjon = new Kontaktinformasjon();
        kontaktinformasjon.setMobiltelefonnummer(applicantInstanceElement.getFieldValue("Kontaktinformasjon.mobiltelefonnummer"));
        kontaktinformasjon.setEpostadresse(applicantInstanceElement.getFieldValue("Kontaktinformasjon.epostadresse"));
        korrespondansepartResource.setKontaktinformasjon(kontaktinformasjon);

        if (Boolean.parseBoolean(applicantInstanceElement.getFieldValue("protected"))) {
            SkjermingResource skjermingResource = new SkjermingResource();
            skjermingResource.addTilgangsrestriksjon(Link.with(applicantInstanceElement.getFieldValue("tilgangsrestriksjon")));
            skjermingResource.addSkjermingshjemmel(Link.with(applicantInstanceElement.getFieldValue("skjermingshjemmel")));
            korrespondansepartResource.setSkjerming(skjermingResource);
        }

        return korrespondansepartResource;
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
        dokumentbeskrivelseResource.setTittel(documentInstanceElement.getFieldValue("tittel") + "_" + document.getName());
        dokumentbeskrivelseResource.addDokumentstatus(Link.with(documentInstanceElement.getFieldValue("dokumentStatus")));

        DokumentobjektResource dokumentobjektResource = new DokumentobjektResource();
        dokumentobjektResource.addVariantFormat(Link.with(
                documentInstanceElement.getFieldValue("DokumentBeskrivelse.dokumentObjekt.variantFormat")
        ));
        // TODO: 25/10/2022 Add file
        dokumentbeskrivelseResource.setDokumentobjekt(List.of(dokumentobjektResource));
        return dokumentbeskrivelseResource;
    }

}
