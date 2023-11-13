package no.fintlabs.dispatch.file;

import no.fintlabs.dispatch.file.result.FilesDispatchResult;
import no.fintlabs.model.File;
import no.fintlabs.model.instance.DokumentbeskrivelseDto;
import no.fintlabs.model.instance.DokumentobjektDto;
import no.fintlabs.web.archive.FintArchiveClient;
import no.fintlabs.web.file.FileClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class FileDispatchServiceTest {

    @Mock
    private FintArchiveClient fintArchiveClient;
    @Mock
    private FileClient fileClient;
    @InjectMocks
    private FileDispatchService fileDispatchService;

    @Test
    public void givenNoDokumentBeskrivelseDtosShouldReturnAcceptedResultWithNoDispatches() {
        StepVerifier.create(
                        fileDispatchService.dispatchFiles(List.of())
                )
                .expectNext(FilesDispatchResult.accepted(Map.of()))
                .verifyComplete();
    }

    @Test
    public void givenSingleDokumentBeskrivelseWithSingleDokumentObjektShouldReturnAcceptedResultWithSingleDispatch() {

        UUID fileId = mock(UUID.class);
        File file = mock(File.class);

        when(fileClient.getFile(fileId)
                .thenReturn(
                        Mono.just(file)
                ));

        StepVerifier.create(
                fileDispatchService.dispatchFiles(List.of(
                        DokumentbeskrivelseDto
                                .builder()
                                .dokumentobjekt(List.of(
                                        DokumentobjektDto
                                                .builder()
                                                .fileId(fileId)
                                                .build()
                                ))
                                .build()
                ))
        ).expectNext(FilesDispatchResult.accepted(

        ))
    }

}