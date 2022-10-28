package no.fintlabs.mapping;

import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.KorrespondansepartResource;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.fintlabs.model.mappedinstance.MappedInstanceElement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ApplicantMappingService {

    private final ShieldingMappingService shieldingMappingService;

    public ApplicantMappingService(ShieldingMappingService shieldingMappingService) {
        this.shieldingMappingService = shieldingMappingService;
    }

    public KorrespondansepartResource toKorrespondansepartResource(MappedInstanceElement applicantInstanceElement) {
        KorrespondansepartResource korrespondansepartResource = new KorrespondansepartResource();

        // TODO: 28/10/2022 Replace with field in configuration
        korrespondansepartResource.addKorrespondanseparttype(Link.with("https://beta.felleskomponent.no/arkiv/kodeverk/korrespondanseparttype/systemid/EA"));

        applicantInstanceElement.getFieldValue("organisasjonsnummer").ifPresent(korrespondansepartResource::setOrganisasjonsnummer);
        applicantInstanceElement.getFieldValue("fødselsnummer").ifPresent(korrespondansepartResource::setFodselsnummer);
        applicantInstanceElement.getFieldValue("KorrespondansepartNavn").ifPresent(korrespondansepartResource::setKorrespondansepartNavn);

        toAdresseResource(applicantInstanceElement).ifPresent(korrespondansepartResource::setAdresse);

        applicantInstanceElement.getFieldValue("kontaktperson").ifPresent(korrespondansepartResource::setKontaktperson);
        toKontaktinformasjon(applicantInstanceElement).ifPresent(korrespondansepartResource::setKontaktinformasjon);

        if (applicantInstanceElement.getFieldValue("protected").map(Boolean::parseBoolean).orElse(false)) {
            shieldingMappingService.toSkjermingResource(applicantInstanceElement).ifPresent(korrespondansepartResource::setSkjerming);
        }

        return korrespondansepartResource;
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

}
