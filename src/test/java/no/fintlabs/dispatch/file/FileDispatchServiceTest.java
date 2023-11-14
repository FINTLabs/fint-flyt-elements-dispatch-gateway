package no.fintlabs.dispatch.file;

import lombok.AllArgsConstructor;
import no.fint.model.resource.Link;
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
        FileMock fileMock = mockFile();

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
        List<FileMock> fileMocks = mockFiles(2);

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
        List<FileMock> fileMocks = mockFiles(2);

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
        List<FileMock> fileMocks = mockFiles(5);

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


    private void verifyGetFileAndPostFile(List<FileMock> fileMocks) {
        IntStream.range(0, fileMocks.size()).forEach(i -> {
            verify(fileClient, times(1).description("Verify error for fileMock with index=" + i))
                    .getFile(fileMocks.get(i).fileId);
            verify(fintArchiveClient, times(1).description("Verify error for fileMock with index=" + i))
                    .postFile(fileMocks.get(i).file);
        });
    }

    private List<FileMock> mockFiles(int numberOfFiles) {
        return IntStream.range(0, numberOfFiles)
                .mapToObj(i -> mockFile())
                .toList();
    }

    private FileMock mockFile() {
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