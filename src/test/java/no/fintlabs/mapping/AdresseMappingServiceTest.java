package no.fintlabs.mapping;

import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.fintlabs.model.instance.AdresseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class AdresseMappingServiceTest {

    private AdresseMappingService adresseMappingService;

    @BeforeEach
    void setUp() {
        adresseMappingService = new AdresseMappingService();
    }

    @Test
    void toAdresseResource() {
        Collection<String> addressLines = Arrays.asList("Line 1", "Line 2");
        String postNumber = "12345";
        String postPlace = "Post Place";

        AdresseDto adresseDto = AdresseDto.builder()
                .adresselinje(addressLines)
                .postnummer(postNumber)
                .poststed(postPlace)
                .build();

        AdresseResource adresseResource = adresseMappingService.toAdresseResource(adresseDto);

        assertThat(adresseResource).isNotNull();
        assertThat(adresseResource.getAdresselinje()).containsExactlyElementsOf(addressLines);
        assertThat(adresseResource.getPostnummer()).isEqualTo(postNumber);
        assertThat(adresseResource.getPoststed()).isEqualTo(postPlace);
    }

    @Test
    void toAdresseResource_NullFields() {
        AdresseDto adresseDto = AdresseDto.builder().build();

        AdresseResource adresseResource = adresseMappingService.toAdresseResource(adresseDto);

        assertThat(adresseResource).isNotNull();
        assertThat(adresseResource.getAdresselinje()).isNull();
        assertThat(adresseResource.getPostnummer()).isNull();
        assertThat(adresseResource.getPoststed()).isNull();
    }
}
