package no.fintlabs.dispatch.file;

import lombok.AllArgsConstructor;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentfilResource;
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
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;
import static org.mockito.Mockito.*;


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
        FileMock fileMock = mockSuccessfulFile();

        DokumentbeskrivelseDto dokumentbeskrivelseDto = DokumentbeskrivelseDto
                .builder()
                .dokumentobjekt(List.of(
                        DokumentobjektDto
                                .builder()
                                .fileId(fileMock.fileId)
                                .build()
                ))
                .build();

        StepVerifier.create(
                        fileDispatchService.dispatchFiles(List.of(dokumentbeskrivelseDto))
                )
                .expectNext(FilesDispatchResult.accepted(Map.of(fileMock.fileId, fileMock.archiveLink)))
                .verifyComplete();

        verify(fileClient, times(1)).getFile(fileMock.fileId);
        verify(fintArchiveClient, times(1)).postFile(fileMock.file);
    }

    @Test
    public void givenSingleDokumentBeskrivelseWithMultipleDokumentObjektShouldReturnAcceptedResultWithMultipleDispatch() {
        List<FileMock> fileMocks = mockSuccessfulFiles(2);

        DokumentbeskrivelseDto dokumentbeskrivelseDto = DokumentbeskrivelseDto
                .builder()
                .dokumentobjekt(List.of(
                        DokumentobjektDto
                                .builder()
                                .fileId(fileMocks.get(0).fileId)
                                .build(),
                        DokumentobjektDto
                                .builder()
                                .fileId(fileMocks.get(1).fileId)
                                .build()
                ))
                .build();

        StepVerifier
                .create(fileDispatchService.dispatchFiles(
                        List.of(dokumentbeskrivelseDto))
                )
                .expectNext(FilesDispatchResult.accepted(
                        Map.of(
                                fileMocks.get(0).fileId, fileMocks.get(0).archiveLink,
                                fileMocks.get(1).fileId, fileMocks.get(1).archiveLink
                        )
                )).verifyComplete();

        verifyGetFileAndPostFile(fileMocks);

        verifyNoMoreInteractions(fileClient);
        verifyNoMoreInteractions(fintArchiveClient);
    }

    @Test
    public void givenMultipleDokumentBeskrivelseWithSingleDokumentObjektShouldReturnAcceptedResultWithMultipleDispatch() {
        List<FileMock> fileMocks = mockSuccessfulFiles(2);

        DokumentbeskrivelseDto dokumentbeskrivelseDto1 = DokumentbeskrivelseDto
                .builder()
                .dokumentobjekt(List.of(
                        DokumentobjektDto
                                .builder()
                                .fileId(fileMocks.get(0).fileId)
                                .build()
                ))
                .build();
        DokumentbeskrivelseDto dokumentbeskrivelseDto2 = DokumentbeskrivelseDto
                .builder()
                .dokumentobjekt(List.of(
                        DokumentobjektDto
                                .builder()
                                .fileId(fileMocks.get(1).fileId)
                                .build()
                ))
                .build();

        StepVerifier.create(
                        fileDispatchService.dispatchFiles(List.of(dokumentbeskrivelseDto1, dokumentbeskrivelseDto2))
                )
                .expectNext(FilesDispatchResult.accepted(
                        Map.of(
                                fileMocks.get(0).fileId, fileMocks.get(0).archiveLink,
                                fileMocks.get(1).fileId, fileMocks.get(1).archiveLink
                        ))
                )
                .verifyComplete();

        verifyGetFileAndPostFile(fileMocks);

        verifyNoMoreInteractions(fileClient);
        verifyNoMoreInteractions(fintArchiveClient);
    }


    @Test
    public void givenMultipleDokumentBeskrivelseWithMultipleDokumentObjektShouldReturnAcceptedResultWithMultipleDispatch() {
        List<FileMock> fileMocks = mockSuccessfulFiles(5);

        DokumentbeskrivelseDto dokumentbeskrivelseDto1 = DokumentbeskrivelseDto
                .builder()
                .dokumentobjekt(List.of(
                        DokumentobjektDto
                                .builder()
                                .fileId(fileMocks.get(0).fileId)
                                .build(),
                        DokumentobjektDto
                                .builder()
                                .fileId(fileMocks.get(1).fileId)
                                .build()
                ))
                .build();
        DokumentbeskrivelseDto dokumentbeskrivelseDto2 = DokumentbeskrivelseDto
                .builder()
                .dokumentobjekt(List.of(
                        DokumentobjektDto
                                .builder()
                                .fileId(fileMocks.get(2).fileId)
                                .build(),
                        DokumentobjektDto
                                .builder()
                                .fileId(fileMocks.get(3).fileId)
                                .build(),
                        DokumentobjektDto
                                .builder()
                                .fileId(fileMocks.get(4).fileId)
                                .build()
                ))
                .build();

        StepVerifier.create(
                        fileDispatchService.dispatchFiles(List.of(
                                dokumentbeskrivelseDto1,
                                dokumentbeskrivelseDto2
                        ))
                ).expectNext(FilesDispatchResult.accepted(fileMocks.stream()
                        .collect(toMap(
                                fileMock -> fileMock.fileId,
                                fileMock -> fileMock.archiveLink
                        ))

                ))
                .verifyComplete();

        verifyGetFileAndPostFile(fileMocks);

        verifyNoMoreInteractions(fileClient);
        verifyNoMoreInteractions(fintArchiveClient);
    }

    @Test
    public void givenErrorFromGetFileShouldReturnFailedResult() {
        UUID fileId = mock(UUID.class);

        doReturn(Mono.error(new RuntimeException())).when(fileClient).getFile(fileId);

        DokumentbeskrivelseDto dokumentbeskrivelseDto = DokumentbeskrivelseDto
                .builder()
                .dokumentobjekt(List.of(
                        DokumentobjektDto
                                .builder()
                                .fileId(fileId)
                                .build()
                ))
                .build();

        StepVerifier
                .create(
                        fileDispatchService.dispatchFiles(List.of(
                                dokumentbeskrivelseDto
                        ))
                )
                .expectNext(FilesDispatchResult.failed(null))
                .verifyComplete();

        verify(fileClient, times(1)).getFile(fileId);
        verifyNoMoreInteractions(fileClient);
    }

    @Test
    public void givenSuccessThenErrorFromGetFileShouldReturnFailedResultWithDispatchedFilesWarning() {
        UUID fileId = mock(UUID.class);
        FileMock fileMock = mockSuccessfulFile();

        doReturn(Mono.error(new RuntimeException())).when(fileClient).getFile(fileId);

        DokumentfilResource dokumentfilResource = mock(DokumentfilResource.class);
        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi("123");
        doReturn(identifikator).when(dokumentfilResource).getSystemId();
        doReturn(Mono.just(dokumentfilResource)).when(fintArchiveClient).getFile(fileMock.archiveLink);

        DokumentbeskrivelseDto dokumentbeskrivelseDto = DokumentbeskrivelseDto
                .builder()
                .dokumentobjekt(List.of(
                        DokumentobjektDto
                                .builder()
                                .fileId(fileMock.fileId)
                                .build(),
                        DokumentobjektDto
                                .builder()
                                .fileId(fileId)
                                .build()
                ))
                .build();

        StepVerifier
                .create(fileDispatchService.dispatchFiles(List.of(
                                dokumentbeskrivelseDto
                        ))
                )
                .expectNext(FilesDispatchResult.failed("dokumentobjekt with id='123'"))
                .verifyComplete();

        verify(fileClient, times(1)).getFile(fileId);
        verify(fileClient, times(1)).getFile(fileMock.fileId);
        verifyNoMoreInteractions(fileClient);

        verify(fintArchiveClient, times(1)).postFile(fileMock.file);
        verify(fintArchiveClient, times(1)).getFile(fileMock.archiveLink);
        verifyNoMoreInteractions(fintArchiveClient);
    }

    @Test
    public void givenWebClientResponseExceptionFromPostFileShouldReturnDeclinedResult() {
        UUID fileId = mock(UUID.class);
        File file = mock(File.class);
        WebClientResponseException webClientResponseException = mock(WebClientResponseException.class);
        doReturn("test response body").when(webClientResponseException).getResponseBodyAsString();

        doReturn(Mono.just(file)).when(fileClient).getFile(fileId);
        doReturn(Mono.error(webClientResponseException)).when(fintArchiveClient).postFile(file);

        DokumentbeskrivelseDto dokumentbeskrivelseDto = DokumentbeskrivelseDto
                .builder()
                .dokumentobjekt(List.of(
                        DokumentobjektDto
                                .builder()
                                .fileId(fileId)
                                .build()
                ))
                .build();

        StepVerifier
                .create(
                        fileDispatchService.dispatchFiles(List.of(
                                dokumentbeskrivelseDto
                        ))
                )
                .expectNext(FilesDispatchResult.declined("test response body", null))
                .verifyComplete();

        verify(fileClient, times(1)).getFile(fileId);
        verifyNoMoreInteractions(fileClient);

        verify(fintArchiveClient, times(1)).postFile(file);
        verifyNoMoreInteractions(fintArchiveClient);
    }

    @Test
    public void givenErrorOtherThanWebClientResponseExceptionFromPostFileShouldReturnFailedResult() {
        UUID fileId = mock(UUID.class);
        File file = mock(File.class);

        doReturn(Mono.just(file)).when(fileClient).getFile(fileId);
        doReturn(Mono.error(new RuntimeException())).when(fintArchiveClient).postFile(file);

        DokumentbeskrivelseDto dokumentbeskrivelseDto = DokumentbeskrivelseDto
                .builder()
                .dokumentobjekt(List.of(
                        DokumentobjektDto
                                .builder()
                                .fileId(fileId)
                                .build()
                ))
                .build();

        StepVerifier
                .create(
                        fileDispatchService.dispatchFiles(List.of(
                                dokumentbeskrivelseDto
                        ))
                )
                .expectNext(FilesDispatchResult.failed(null))
                .verifyComplete();

        verify(fileClient, times(1)).getFile(fileId);
        verifyNoMoreInteractions(fileClient);

        verify(fintArchiveClient, times(1)).postFile(file);
        verifyNoMoreInteractions(fintArchiveClient);
    }

    @Test
    public void givenSuccessThenWebclientResponseExceptionFromPostFileShouldReturnDeclinedResultWithDispatchedFilesWarning() {
        List<FileMock> successfulFiles = mockSuccessfulFiles(2);

        UUID errorFileId = mock(UUID.class);
        File errorFile = mock(File.class);

        WebClientResponseException webClientResponseException = mock(WebClientResponseException.class);
        doReturn("test response body").when(webClientResponseException).getResponseBodyAsString();

        DokumentfilResource dokumentfilResource1 = mock(DokumentfilResource.class);
        Identifikator identifikator1 = new Identifikator();
        identifikator1.setIdentifikatorverdi("123");
        doReturn(identifikator1).when(dokumentfilResource1).getSystemId();
        doReturn(Mono.just(dokumentfilResource1)).when(fintArchiveClient).getFile(successfulFiles.get(0).archiveLink);

        DokumentfilResource dokumentfilResource2 = mock(DokumentfilResource.class);
        Identifikator identifikator2 = new Identifikator();
        identifikator2.setIdentifikatorverdi("456");
        doReturn(identifikator2).when(dokumentfilResource2).getSystemId();
        doReturn(Mono.just(dokumentfilResource2)).when(fintArchiveClient).getFile(successfulFiles.get(1).archiveLink);

        doReturn(Mono.just(errorFile)).when(fileClient).getFile(errorFileId);
        doReturn(Mono.error(webClientResponseException)).when(fintArchiveClient).postFile(errorFile);

        DokumentbeskrivelseDto dokumentbeskrivelseDto = DokumentbeskrivelseDto
                .builder()
                .dokumentobjekt(List.of(
                        DokumentobjektDto
                                .builder()
                                .fileId(successfulFiles.get(0).fileId)
                                .build(),
                        DokumentobjektDto
                                .builder()
                                .fileId(successfulFiles.get(1).fileId)
                                .build(),
                        DokumentobjektDto
                                .builder()
                                .fileId(errorFileId)
                                .build()
                ))
                .build();

        StepVerifier
                .create(
                        fileDispatchService.dispatchFiles(List.of(
                                dokumentbeskrivelseDto
                        ))
                )
                .expectNext(FilesDispatchResult.declined("test response body", "dokumentobjekts with ids=['123', '456']"))
                .verifyComplete();

        verifyGetFileAndPostFile(successfulFiles);

        verify(fileClient, times(1)).getFile(errorFileId);
        verifyNoMoreInteractions(fileClient);

        verify(fintArchiveClient, times(1)).postFile(errorFile);
        verifyNoMoreInteractions(fintArchiveClient);
    }

    @Test
    public void givenSuccessThenErrorOtherThanWebclientResponseExceptionFromPostFileShouldReturnFailedResultWithDispatchedFilesWarning() {
        List<FileMock> successfulFiles = mockSuccessfulFiles(2);

        UUID errorFileId = mock(UUID.class);
        File errorFile = mock(File.class);


        DokumentfilResource dokumentfilResource1 = mock(DokumentfilResource.class);
        Identifikator identifikator1 = new Identifikator();
        identifikator1.setIdentifikatorverdi("123");
        doReturn(identifikator1).when(dokumentfilResource1).getSystemId();
        doReturn(Mono.just(dokumentfilResource1)).when(fintArchiveClient).getFile(successfulFiles.get(0).archiveLink);

        DokumentfilResource dokumentfilResource2 = mock(DokumentfilResource.class);
        Identifikator identifikator2 = new Identifikator();
        identifikator2.setIdentifikatorverdi("456");
        doReturn(identifikator2).when(dokumentfilResource2).getSystemId();
        doReturn(Mono.just(dokumentfilResource2)).when(fintArchiveClient).getFile(successfulFiles.get(1).archiveLink);

        doReturn(Mono.just(errorFile)).when(fileClient).getFile(errorFileId);
        doReturn(Mono.error(new RuntimeException())).when(fintArchiveClient).postFile(errorFile);

        DokumentbeskrivelseDto dokumentbeskrivelseDto = DokumentbeskrivelseDto
                .builder()
                .dokumentobjekt(List.of(
                        DokumentobjektDto
                                .builder()
                                .fileId(successfulFiles.get(0).fileId)
                                .build(),
                        DokumentobjektDto
                                .builder()
                                .fileId(successfulFiles.get(1).fileId)
                                .build(),
                        DokumentobjektDto
                                .builder()
                                .fileId(errorFileId)
                                .build()
                ))
                .build();

        StepVerifier
                .create(
                        fileDispatchService.dispatchFiles(List.of(
                                dokumentbeskrivelseDto
                        ))
                )
                .expectNext(FilesDispatchResult.failed("dokumentobjekts with ids=['123', '456']"))
                .verifyComplete();

        verifyGetFileAndPostFile(successfulFiles);

        verify(fileClient, times(1)).getFile(errorFileId);
        verifyNoMoreInteractions(fileClient);

        verify(fintArchiveClient, times(1)).postFile(errorFile);
        verifyNoMoreInteractions(fintArchiveClient);
    }

    @Test
    public void givenEmptyFileLinkListCreateFunctionalWarningMessageShouldReturnOptionalEmpty() {
        StepVerifier.create(
                        fileDispatchService.createFunctionalWarningMessage(List.of())
                )
                .expectNextMatches(Optional::isEmpty)
                .verifyComplete();
    }

    @Test
    public void givenFileLinksAndSuccessfulResponseFromGetFileCreateFunctionalWarningMessageShouldReturnOptionalStringWithFileIds() {
        Link link = mock(Link.class);

        DokumentfilResource dokumentfilResource = mock(DokumentfilResource.class);
        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi("123");
        doReturn(identifikator).when(dokumentfilResource).getSystemId();
        doReturn(Mono.just(dokumentfilResource)).when(fintArchiveClient).getFile(link);

        StepVerifier.create(
                        fileDispatchService.createFunctionalWarningMessage(List.of(
                                link
                        ))
                )
                .expectNext(Optional.of("dokumentobjekt with id='123'"))
                .verifyComplete();
    }

    @Test
    public void givenFileLinksAndErrorResponseFromGetFileCreateFunctionalWarningMessageShouldReturnOptionalStringWithFileLinks() {
        Link link = mock(Link.class);

        doReturn("https://www.testlink.com").when(link).getHref();
        doReturn(Mono.error(new RuntimeException())).when(fintArchiveClient).getFile(link);

        StepVerifier.create(
                        fileDispatchService.createFunctionalWarningMessage(List.of(
                                link
                        ))
                )
                .expectNext(Optional.of("dokumentobjekt with link='https://www.testlink.com'"))
                .verifyComplete();
    }

    private void verifyGetFileAndPostFile(List<FileMock> fileMocks) {
        IntStream.range(0, fileMocks.size()).forEach(i -> {
            verify(fileClient, times(1).description("Verify error for fileMock with index=" + i))
                    .getFile(fileMocks.get(i).fileId);
            verify(fintArchiveClient, times(1).description("Verify error for fileMock with index=" + i))
                    .postFile(fileMocks.get(i).file);
        });
    }

    private List<FileMock> mockSuccessfulFiles(int numberOfFiles) {
        return IntStream.range(0, numberOfFiles)
                .mapToObj(i -> mockSuccessfulFile())
                .toList();
    }

    private FileMock mockSuccessfulFile() {
        FileMock fileMock = new FileMock(
                mock(UUID.class),
                mock(File.class),
                mock(Link.class)
        );
        doReturn(Mono.just(fileMock.file)).when(fileClient).getFile(fileMock.fileId);
        doReturn(Mono.just(fileMock.archiveLink)).when(fintArchiveClient).postFile(fileMock.file);

        return fileMock;
    }

    @AllArgsConstructor
    private static class FileMock {
        private final UUID fileId;
        private final File file;
        private final Link archiveLink;
    }

}