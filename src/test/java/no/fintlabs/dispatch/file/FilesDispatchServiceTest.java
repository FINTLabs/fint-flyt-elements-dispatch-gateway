package no.fintlabs.dispatch.file;

import lombok.AllArgsConstructor;
import no.fint.model.resource.Link;
import no.fintlabs.dispatch.file.result.FileDispatchResult;
import no.fintlabs.dispatch.file.result.FilesDispatchResult;
import no.fintlabs.model.instance.DokumentobjektDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilesDispatchServiceTest {

    @Mock
    private FileDispatchService fileDispatchService;
    @Mock
    private FilesWarningMessageService filesWarningMessageService;
    @InjectMocks
    private FilesDispatchService filesDispatchService;
    private Random random;

    @BeforeEach
    public void setup() {
        random = new Random(42);
    }

    @Test
    public void givenNoDokumentBeskrivelseDtosShouldReturnAcceptedResultWithNoDispatches() {
        StepVerifier.create(
                        filesDispatchService.dispatch(List.of())
                )
                .expectNext(FilesDispatchResult.accepted(Map.of()))
                .verifyComplete();
    }

    @Test
    public void givenAcceptedDispatchOfSingleDokumentobjektShouldReturnAcceptedResultWithSingleDispatch() {
        DokumentobjektMock dokumentobjektMock = mockDokumentobjektWithAcceptedDispatch();

        StepVerifier
                .create(filesDispatchService.dispatch(List.of(dokumentobjektMock.dokumentobjektDto)))
                .expectNext(FilesDispatchResult.accepted(Map.of(dokumentobjektMock.fileId, dokumentobjektMock.archiveLink)))
                .verifyComplete();

        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock.dokumentobjektDto);
        verifyNoMoreInteractions(fileDispatchService);
    }

    @Test
    public void givenAcceptedDispatchOfMultipleDokumentobjektShouldReturnAcceptedResultWithMultipleDispatch() {
        DokumentobjektMock dokumentobjektMock1 = mockDokumentobjektWithAcceptedDispatch();
        DokumentobjektMock dokumentobjektMock2 = mockDokumentobjektWithAcceptedDispatch();

        StepVerifier.create(
                        filesDispatchService.dispatch(List.of(
                                dokumentobjektMock1.dokumentobjektDto,
                                dokumentobjektMock2.dokumentobjektDto
                        ))
                )
                .expectNext(FilesDispatchResult.accepted(
                        Map.of(
                                dokumentobjektMock1.fileId, dokumentobjektMock1.archiveLink,
                                dokumentobjektMock2.fileId, dokumentobjektMock2.archiveLink
                        ))
                )
                .verifyComplete();

        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock1.dokumentobjektDto);
        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock2.dokumentobjektDto);
        verifyNoMoreInteractions(fileDispatchService);
    }

    @Test
    public void givenDeclinedDispatchOfSingleDokumentobjektShouldReturnDeclinedResultWithNoWarningMessage() {
        DokumentobjektMock dokumentobjektMock = mockDokumentobjektWithDeclinedDispatch("test error message");

        StepVerifier.create(filesDispatchService.dispatch(List.of(dokumentobjektMock.dokumentobjektDto)))
                .expectNext(FilesDispatchResult.declined("test error message", null))
                .verifyComplete();

        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock.dokumentobjektDto);
        verifyNoMoreInteractions(fileDispatchService);
    }

    @Test
    public void givenDeclinedDispatchOfFirstOfMultipleDokumentobjektShouldReturnDeclinedResultWithNoWarningMessage() {
        DokumentobjektMock dokumentobjektMock1 = mockDokumentobjektWithDeclinedDispatch("test error message");
        DokumentobjektMock dokumentobjektMock2 = mockDokumentobjekt();
        DokumentobjektMock dokumentobjektMock3 = mockDokumentobjekt();

        StepVerifier.create(filesDispatchService.dispatch(List.of(
                        dokumentobjektMock1.dokumentobjektDto,
                        dokumentobjektMock2.dokumentobjektDto,
                        dokumentobjektMock3.dokumentobjektDto
                )))
                .expectNext(FilesDispatchResult.declined("test error message", null))
                .verifyComplete();

        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock1.dokumentobjektDto);
        verifyNoMoreInteractions(fileDispatchService);
    }

    @Test
    public void givenDeclinedDispatchOfLastOfMultipleDokumentobjektShouldReturnDeclinedResultWithWarningMessage() {
        DokumentobjektMock dokumentobjektMock1 = mockDokumentobjektWithAcceptedDispatch();
        DokumentobjektMock dokumentobjektMock2 = mockDokumentobjektWithAcceptedDispatch();
        DokumentobjektMock dokumentobjektMock3 = mockDokumentobjektWithDeclinedDispatch("test error message");

        doReturn(Mono.just(Optional.of("test warning message")))
                .when(filesWarningMessageService).createFunctionalWarningMessage(List.of(
                        dokumentobjektMock1.archiveLink,
                        dokumentobjektMock2.archiveLink
                ));

        StepVerifier.create(filesDispatchService.dispatch(List.of(
                        dokumentobjektMock1.dokumentobjektDto,
                        dokumentobjektMock2.dokumentobjektDto,
                        dokumentobjektMock3.dokumentobjektDto
                )))
                .expectNext(FilesDispatchResult.declined(
                        "test error message",
                        "test warning message"
                ))
                .verifyComplete();

        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock1.dokumentobjektDto);
        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock2.dokumentobjektDto);
        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock3.dokumentobjektDto);
        verifyNoMoreInteractions(fileDispatchService);

        verify(filesWarningMessageService, times(1)).createFunctionalWarningMessage(List.of(
                        dokumentobjektMock1.archiveLink,
                        dokumentobjektMock2.archiveLink
                )
        );
        verifyNoMoreInteractions(filesWarningMessageService);
    }


    @Test
    public void givenFailedDispatchOfSingleDokumentobjektShouldReturnFailedResultWithNoWarningMessage() {
        DokumentobjektMock dokumentobjektMock = mockDokumentobjektWithFailedDispatch();

        StepVerifier.create(filesDispatchService.dispatch(List.of(dokumentobjektMock.dokumentobjektDto)))
                .expectNext(FilesDispatchResult.failed(null))
                .verifyComplete();

        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock.dokumentobjektDto);
        verifyNoMoreInteractions(fileDispatchService);
    }

    @Test
    public void givenFailedDispatchOfFirstOfMultipleDokumentobjektShouldReturnFailedResultWithNoWarningMessage() {
        DokumentobjektMock dokumentobjektMock1 = mockDokumentobjektWithFailedDispatch();
        DokumentobjektMock dokumentobjektMock2 = mockDokumentobjekt();
        DokumentobjektMock dokumentobjektMock3 = mockDokumentobjekt();

        StepVerifier.create(filesDispatchService.dispatch(List.of(
                        dokumentobjektMock1.dokumentobjektDto,
                        dokumentobjektMock2.dokumentobjektDto,
                        dokumentobjektMock3.dokumentobjektDto
                )))
                .expectNext(FilesDispatchResult.failed(null))
                .verifyComplete();

        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock1.dokumentobjektDto);
        verifyNoMoreInteractions(fileDispatchService);
    }

    @Test
    public void givenFailedDispatchOfLastOfMultipleDokumentobjektShouldReturnFailedResultWithWarningMessage() {
        DokumentobjektMock dokumentobjektMock1 = mockDokumentobjektWithAcceptedDispatch();
        DokumentobjektMock dokumentobjektMock2 = mockDokumentobjektWithAcceptedDispatch();
        DokumentobjektMock dokumentobjektMock3 = mockDokumentobjektWithFailedDispatch();

        doReturn(Mono.just(Optional.of("formatted warning message")))
                .when(filesWarningMessageService).createFunctionalWarningMessage(List.of(
                        dokumentobjektMock1.archiveLink,
                        dokumentobjektMock2.archiveLink
                ));

        StepVerifier.create(filesDispatchService.dispatch(List.of(
                        dokumentobjektMock1.dokumentobjektDto,
                        dokumentobjektMock2.dokumentobjektDto,
                        dokumentobjektMock3.dokumentobjektDto
                )))
                .expectNext(FilesDispatchResult.failed(
                        "formatted warning message"
                ))
                .verifyComplete();

        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock1.dokumentobjektDto);
        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock2.dokumentobjektDto);
        verify(fileDispatchService, times(1)).dispatch(dokumentobjektMock3.dokumentobjektDto);
        verifyNoMoreInteractions(fileDispatchService);

        verify(filesWarningMessageService, times(1)).createFunctionalWarningMessage(List.of(
                dokumentobjektMock1.archiveLink,
                dokumentobjektMock2.archiveLink
        ));
        verifyNoMoreInteractions(filesWarningMessageService);
    }

    private DokumentobjektMock mockDokumentobjektWithAcceptedDispatch() {
        DokumentobjektMock dokumentobjektMock = mockDokumentobjekt();
        doReturn(Mono.just(FileDispatchResult.accepted(dokumentobjektMock.fileId, dokumentobjektMock.archiveLink)))
                .when(fileDispatchService).dispatch(dokumentobjektMock.dokumentobjektDto);
        return dokumentobjektMock;
    }

    private DokumentobjektMock mockDokumentobjektWithDeclinedDispatch(String errorMessage) {
        DokumentobjektMock dokumentobjektMock = mockDokumentobjekt();
        doReturn(Mono.just(FileDispatchResult.declined(dokumentobjektMock.fileId, errorMessage)))
                .when(fileDispatchService).dispatch(dokumentobjektMock.dokumentobjektDto);
        return dokumentobjektMock;
    }

    private DokumentobjektMock mockDokumentobjektWithFailedDispatch() {
        DokumentobjektMock dokumentobjektMock = mockDokumentobjekt();
        doReturn(Mono.just(FileDispatchResult.failed(dokumentobjektMock.fileId)))
                .when(fileDispatchService).dispatch(dokumentobjektMock.dokumentobjektDto);
        return dokumentobjektMock;
    }

    private DokumentobjektMock mockDokumentobjekt() {
        UUID fileId = getUuid();
        return new DokumentobjektMock(
                fileId,
                mock(Link.class),
                DokumentobjektDto.builder().fileId(fileId).build()
        );
    }

    @AllArgsConstructor
    private static class DokumentobjektMock {
        private final UUID fileId;
        private final Link archiveLink;
        private final DokumentobjektDto dokumentobjektDto;
    }

    private UUID getUuid() {
        byte[] bytes = new byte[7];
        random.nextBytes(bytes);
        return UUID.nameUUIDFromBytes(bytes);
    }


}