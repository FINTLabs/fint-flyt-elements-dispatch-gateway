package no.fintlabs.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fintlabs.model.instance.DokumentbeskrivelseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DokumentbeskrivelseMappingServiceTest {

    @InjectMocks
    private DokumentbeskrivelseMappingService dokumentbeskrivelseMappingService;

    @Mock
    private DokumentObjektMappingService dokumentObjektMappingService;

    @Mock
    private SkjermingMappingService skjermingMappingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toDokumentbeskrivelseResource() {
        DokumentbeskrivelseDto dto = DokumentbeskrivelseDto.builder()
                .tittel("Test Title")
                .dokumentstatus("Status")
                .dokumentType("Type")
                .tilknyttetRegistreringSom("Tilknyttet")
                .build();

        HashMap<UUID, Link> linkMap = new HashMap<>();

        DokumentbeskrivelseResource resource = dokumentbeskrivelseMappingService.toDokumentbeskrivelseResource(dto, linkMap);

        assertEquals("Test Title", resource.getTittel());
        assertEquals(Collections.singletonList(Link.with("Type")), resource.getDokumentType());
        assertEquals(Collections.singletonList(Link.with("Status")), resource.getDokumentstatus());
        assertEquals(Collections.singletonList(Link.with("Tilknyttet")), resource.getTilknyttetRegistreringSom());
    }
}
