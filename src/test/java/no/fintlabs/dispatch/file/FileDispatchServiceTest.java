package no.fintlabs.dispatch.file;

import lombok.AllArgsConstructor;
import no.fint.model.resource.Link;
import no.fintlabs.dispatch.file.result.FileDispatchResult;
import no.fintlabs.model.File;
import no.fintlabs.model.instance.DokumentobjektDto;
import no.fintlabs.web.archive.FintArchiveClient;
import no.fintlabs.web.file.FileClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Random;
import java.util.UUID;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class FileDispatchServiceTest {

    @Mock
    private FintArchiveClient fintArchiveClient;
    @Mock
    private FileClient fileClient;
    @InjectMocks
    private FileDispatchService fileDispatchService;
    private Random random;

    @BeforeEach
    public void setup() {
        random = new Random(42);
    }

    @Test
    public void givenSuccessFromGetFileAndSuccessFromPostFileShouldReturnAcceptedResult() {
        FileMock fileMock = mockFile();

        doReturn(Mono.just(fileMock.file)).when(fileClient).getFile(fileMock.fileId);
        doReturn(Mono.just(fileMock.archiveLink)).when(fintArchiveClient).postFile(fileMock.file);

        StepVerifier
                .create(fileDispatchService.dispatch(fileMock.dokumentobjektDto))
                .expectNext(FileDispatchResult.accepted(fileMock.fileId, fileMock.archiveLink))
                .verifyComplete();

        verify(fileClient, times(1)).getFile(fileMock.fileId);
        verifyNoMoreInteractions(fileClient);

        verify(fintArchiveClient, times(1)).postFile(fileMock.file);
        verifyNoMoreInteractions(fintArchiveClient);
    }

    @Test
    public void givenErrorFromGetFileShouldReturnFailedCouldNotBeRetrievedResult() {
        UUID fileId = getUuid();

        doReturn(Mono.error(new RuntimeException())).when(fileClient).getFile(fileId);

        DokumentobjektDto dokumentobjektDto = DokumentobjektDto
                .builder()
                .fileId(fileId)
                .build();

        StepVerifier
                .create(fileDispatchService.dispatch(dokumentobjektDto))
                .expectNext(FileDispatchResult.couldNotBeRetrieved(fileId))
                .verifyComplete();

        verify(fileClient, times(1)).getFile(fileId);
        verifyNoMoreInteractions(fileClient);
    }

    @Test
    public void givenSuccessFromGetFileAndWebClientResponseExceptionFromPostFileShouldReturnDeclinedResult() {
        FileMock fileMock = mockFile();

        doReturn(Mono.just(fileMock.file)).when(fileClient).getFile(fileMock.fileId);

        WebClientResponseException webClientResponseException = mock(WebClientResponseException.class);
        doReturn("test response body").when(webClientResponseException).getResponseBodyAsString();
        doReturn(Mono.error(webClientResponseException)).when(fintArchiveClient).postFile(fileMock.file);

        StepVerifier
                .create(fileDispatchService.dispatch(fileMock.dokumentobjektDto))
                .expectNext(FileDispatchResult.declined(fileMock.fileId, "test response body"))
                .verifyComplete();

        verify(fileClient, times(1)).getFile(fileMock.fileId);
        verifyNoMoreInteractions(fileClient);

        verify(fintArchiveClient, times(1)).postFile(fileMock.file);
        verifyNoMoreInteractions(fintArchiveClient);
    }

    @Test
    public void givenSuccessFromGetFileAndErrorOtherThanWebClientResponseExceptionFromPostFileShouldReturnFailedResult() {
        FileMock fileMock = mockFile();

        doReturn(Mono.just(fileMock.file)).when(fileClient).getFile(fileMock.fileId);
        doReturn(Mono.error(new RuntimeException())).when(fintArchiveClient).postFile(fileMock.file);

        StepVerifier
                .create(fileDispatchService.dispatch(fileMock.dokumentobjektDto))
                .expectNext(FileDispatchResult.failed(fileMock.fileId))
                .verifyComplete();

        verify(fileClient, times(1)).getFile(fileMock.fileId);
        verifyNoMoreInteractions(fileClient);

        verify(fintArchiveClient, times(1)).postFile(fileMock.file);
        verifyNoMoreInteractions(fintArchiveClient);
    }

    private FileMock mockFile() {
        UUID fileId = getUuid();
        return new FileMock(
                fileId,
                mock(File.class),
                mock(Link.class),
                DokumentobjektDto.builder().fileId(fileId).build()
        );
    }

    @AllArgsConstructor
    private static class FileMock {
        private final UUID fileId;
        private final File file;
        private final Link archiveLink;
        private final DokumentobjektDto dokumentobjektDto;
    }

    private UUID getUuid() {
        byte[] bytes = new byte[7];
        random.nextBytes(bytes);
        return UUID.nameUUIDFromBytes(bytes);
    }

}