package no.fintlabs.dispatch.journalpost;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fintlabs.dispatch.file.FilesDispatchService;
import no.fintlabs.dispatch.file.FilesWarningMessageService;
import no.fintlabs.dispatch.file.result.FilesDispatchResult;
import no.fintlabs.dispatch.journalpost.result.RecordDispatchResult;
import no.fintlabs.mapping.JournalpostMappingService;
import no.fintlabs.model.instance.DokumentbeskrivelseDto;
import no.fintlabs.model.instance.DokumentobjektDto;
import no.fintlabs.model.instance.JournalpostDto;
import no.fintlabs.web.archive.FintArchiveClient;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.hamcrest.MockitoHamcrest;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordDispatchServiceTest {

    @Mock
    JournalpostMappingService journalpostMappingService;

    @Mock
    FilesDispatchService filesDispatchService;

    @Mock
    FilesWarningMessageService filesWarningMessageService;

    @Mock
    FintArchiveClient fintArchiveClient;

    @InjectMocks
    RecordDispatchService recordDispatchService;

    @Test
    public void givenJournalpostDtoWithNoDokumentbeskrivelseDtosShouldCallMappingServiceWithEmptyDokumentobjektList() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(Optional.empty()).when(journalpostDto).getDokumentbeskrivelse();

        doReturn(Mono.empty()).when(fintArchiveClient).postRecord(any(), any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .verifyComplete();

        verify(journalpostMappingService, times(1))
                .toJournalpostResource(journalpostDto, Map.of());
        verifyNoMoreInteractions(journalpostMappingService);
    }

    @Test
    public void givenJournalpostDtoWithDokumentbeskrivelseDtosShouldDispatchDokumentobjektDtos() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        DokumentobjektDto dokumentobjektDto1 = mock(DokumentobjektDto.class);
        DokumentobjektDto dokumentobjektDto2 = mock(DokumentobjektDto.class);
        DokumentobjektDto dokumentobjektDto3 = mock(DokumentobjektDto.class);
        doReturn(Optional.of(List.of(
                DokumentbeskrivelseDto
                        .builder()
                        .dokumentobjekt(List.of(dokumentobjektDto1, dokumentobjektDto2))
                        .build(),
                DokumentbeskrivelseDto
                        .builder()
                        .dokumentobjekt(List.of(dokumentobjektDto3))
                        .build()
        ))).when(journalpostDto).getDokumentbeskrivelse();

        doReturn(Mono.empty()).when(filesDispatchService).dispatch(any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .verifyComplete();

        verify(filesDispatchService, times(1))
                .dispatch(List.of(dokumentobjektDto1, dokumentobjektDto2, dokumentobjektDto3));
        verifyNoMoreInteractions(filesDispatchService);
    }

    @Test
    public void givenAcceptedFilesDispatchShouldCallMappingServiceWithArchiveFileLinkPerFileId() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        DokumentobjektDto dokumentobjektDto1 = mock(DokumentobjektDto.class);
        DokumentobjektDto dokumentobjektDto2 = mock(DokumentobjektDto.class);
        DokumentobjektDto dokumentobjektDto3 = mock(DokumentobjektDto.class);
        doReturn(Optional.of(List.of(
                DokumentbeskrivelseDto
                        .builder()
                        .dokumentobjekt(List.of(dokumentobjektDto1, dokumentobjektDto2))
                        .build(),
                DokumentbeskrivelseDto
                        .builder()
                        .dokumentobjekt(List.of(dokumentobjektDto3))
                        .build()
        ))).when(journalpostDto).getDokumentbeskrivelse();

        UUID uuid1 = mock(UUID.class);
        UUID uuid2 = mock(UUID.class);
        UUID uuid3 = mock(UUID.class);

        Link link1 = mock(Link.class);
        Link link2 = mock(Link.class);
        Link link3 = mock(Link.class);

        doReturn(
                Mono.just(FilesDispatchResult.accepted(Map.of(
                        uuid1, link1,
                        uuid2, link2,
                        uuid3, link3
                )))
        ).when(filesDispatchService).dispatch(any());

        doReturn(Mono.empty()).when(fintArchiveClient).postRecord(any(), any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .verifyComplete();

        verify(journalpostMappingService, times(1))
                .toJournalpostResource(journalpostDto, Map.of(
                        uuid1, link1,
                        uuid2, link2,
                        uuid3, link3
                ));
        verifyNoMoreInteractions(journalpostMappingService);
    }

    @Test
    public void givenCaseIdAndJournalpostResourceFromMapperShouldCallPostRecordWithCaseIdAndJournalpostResource() {
        String caseId = "testCaseId";

        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(Optional.empty()).when(journalpostDto).getDokumentbeskrivelse();

        JournalpostResource journalpostResource = mock(JournalpostResource.class);
        doReturn(journalpostResource).when(journalpostMappingService).toJournalpostResource(any(), any());

        doReturn(Mono.empty()).when(fintArchiveClient).postRecord(any(), any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .verifyComplete();

        verify(fintArchiveClient, times(1))
                .postRecord(caseId, journalpostResource);
        verifyNoMoreInteractions(fintArchiveClient);
    }

    @Test
    public void givenSuccessfulArchiveClientPostRecordShouldReturnAcceptedResultWithJournalpostnummer() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(Optional.empty()).when(journalpostDto).getDokumentbeskrivelse();

        JournalpostResource journalpostResource = mock(JournalpostResource.class);
        doReturn(journalpostResource).when(journalpostMappingService).toJournalpostResource(any(), any());

        JournalpostResource journalpostResourcePostResult = mock(JournalpostResource.class);
        doReturn(123L).when(journalpostResourcePostResult).getJournalPostnummer();
        doReturn(Mono.just(journalpostResourcePostResult)).when(fintArchiveClient).postRecord(any(), any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .expectNext(RecordDispatchResult.accepted(123L))
                .verifyComplete();
    }

    @Test
    public void givenWebclientResponseExceptionFromArchiveClientPostRecordShouldReturnDeclinedResultWithErrorMessageAndWarningMessage() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(
                Optional.of(List.of(
                        DokumentbeskrivelseDto
                                .builder()
                                .dokumentobjekt(List.of(mock(DokumentobjektDto.class)))
                                .build()
                ))
        ).when(journalpostDto).getDokumentbeskrivelse();

        UUID fileId1 = mock(UUID.class);
        UUID fileId2 = mock(UUID.class);
        Link archiveFileLink1 = mock(Link.class);
        Link archiveFileLink2 = mock(Link.class);
        doReturn(
                Mono.just(FilesDispatchResult.accepted(Map.of(
                        fileId1, archiveFileLink1,
                        fileId2, archiveFileLink2
                )))
        ).when(filesDispatchService).dispatch(any());

        JournalpostResource journalpostResource = mock(JournalpostResource.class);
        doReturn(journalpostResource).when(journalpostMappingService).toJournalpostResource(any(), any());

        WebClientResponseException webClientResponseException = mock(WebClientResponseException.class);
        doReturn("test response body").when(webClientResponseException).getResponseBodyAsString();
        doReturn(Mono.error(webClientResponseException)).when(fintArchiveClient).postRecord(any(), any());

        doReturn(Mono.just(Optional.of("test warning message")))
                .when(filesWarningMessageService).createFunctionalWarningMessage(any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .expectNext(RecordDispatchResult.declined(
                        "test response body",
                        "test warning message"
                ))
                .verifyComplete();

        verify(filesWarningMessageService, times(1)).createFunctionalWarningMessage(
                MockitoHamcrest.argThat(allOf(
                        Matchers.<Link>iterableWithSize(2),
                        Matchers.containsInAnyOrder(archiveFileLink1, archiveFileLink2)
                ))
        );
        verifyNoMoreInteractions(filesWarningMessageService);
    }

    @Test
    public void givenExceptionOtherThanWebclientResponseExceptionFromArchiveClientPostRecordShouldReturnFailedResultWithErrorMessageAndWarningMessage() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(
                Optional.of(List.of(
                        DokumentbeskrivelseDto
                                .builder()
                                .dokumentobjekt(List.of(mock(DokumentobjektDto.class)))
                                .build()
                ))
        ).when(journalpostDto).getDokumentbeskrivelse();

        UUID fileId1 = mock(UUID.class);
        UUID fileId2 = mock(UUID.class);
        Link archiveFileLink1 = mock(Link.class);
        Link archiveFileLink2 = mock(Link.class);
        doReturn(
                Mono.just(FilesDispatchResult.accepted(Map.of(
                        fileId1, archiveFileLink1,
                        fileId2, archiveFileLink2
                )))
        ).when(filesDispatchService).dispatch(any());

        JournalpostResource journalpostResource = mock(JournalpostResource.class);
        doReturn(journalpostResource).when(journalpostMappingService).toJournalpostResource(any(), any());

        doReturn(Mono.error(new RuntimeException())).when(fintArchiveClient).postRecord(any(), any());

        doReturn(Mono.just(Optional.of("test warning message")))
                .when(filesWarningMessageService).createFunctionalWarningMessage(any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .expectNext(RecordDispatchResult.failed(
                        null,
                        "test warning message"
                ))
                .verifyComplete();

        verify(filesWarningMessageService, times(1)).createFunctionalWarningMessage(
                MockitoHamcrest.argThat(allOf(
                        Matchers.<Link>iterableWithSize(2),
                        Matchers.containsInAnyOrder(archiveFileLink1, archiveFileLink2)
                ))
        );
        verifyNoMoreInteractions(filesWarningMessageService);
    }

    @Test
    public void givenDeclinedFilesDispatchWithErrorMessageAndWarningMessageShouldReturnDeclinedResultWithErrorMessageAndWarningMessage() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        DokumentobjektDto dokumentobjektDto1 = mock(DokumentobjektDto.class);
        doReturn(Optional.of(List.of(
                DokumentbeskrivelseDto
                        .builder()
                        .dokumentobjekt(List.of(dokumentobjektDto1))
                        .build()
        ))).when(journalpostDto).getDokumentbeskrivelse();

        doReturn(
                Mono.just(FilesDispatchResult.declined(
                        "test error message",
                        "test functional warning message"
                ))
        ).when(filesDispatchService).dispatch(any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .expectNext(RecordDispatchResult.declined(
                        "Dokumentobjekt declined by destination with message='test error message'",
                        "test functional warning message"
                ))
                .verifyComplete();
    }

    @Test
    public void givenDeclinedFilesDispatchWithErrorMessageAndNoWarningMessageShouldReturnDeclinedResultWithErrorMessageAndNoWarningMessage() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        DokumentobjektDto dokumentobjektDto1 = mock(DokumentobjektDto.class);
        doReturn(Optional.of(List.of(
                DokumentbeskrivelseDto
                        .builder()
                        .dokumentobjekt(List.of(dokumentobjektDto1))
                        .build()
        ))).when(journalpostDto).getDokumentbeskrivelse();

        doReturn(
                Mono.just(FilesDispatchResult.declined(
                        "test error message",
                        null
                ))
        ).when(filesDispatchService).dispatch(any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .expectNext(RecordDispatchResult.declined(
                        "Dokumentobjekt declined by destination with message='test error message'",
                        null
                ))
                .verifyComplete();
    }

    @Test
    public void givenFailedFilesDispatchWithWarningMessageShouldReturnDeclinedResultWithErrorMessageAndWarningMessage() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        DokumentobjektDto dokumentobjektDto1 = mock(DokumentobjektDto.class);
        doReturn(Optional.of(List.of(
                DokumentbeskrivelseDto
                        .builder()
                        .dokumentobjekt(List.of(dokumentobjektDto1))
                        .build()
        ))).when(journalpostDto).getDokumentbeskrivelse();

        doReturn(
                Mono.just(FilesDispatchResult.failed("test functional warning message"))
        ).when(filesDispatchService).dispatch(any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .expectNext(RecordDispatchResult.failed(
                        "Dokumentobjekt dispatch failed",
                        "test functional warning message"
                ))
                .verifyComplete();
    }

    @Test
    public void givenFailedFilesDispatchWithNoWarningMessageShouldReturnDeclinedResultWithErrorMessageAndNoWarningMessage() {
        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        DokumentobjektDto dokumentobjektDto1 = mock(DokumentobjektDto.class);
        doReturn(Optional.of(List.of(
                DokumentbeskrivelseDto
                        .builder()
                        .dokumentobjekt(List.of(dokumentobjektDto1))
                        .build()
        ))).when(journalpostDto).getDokumentbeskrivelse();

        doReturn(
                Mono.just(FilesDispatchResult.failed(null))
        ).when(filesDispatchService).dispatch(any());

        StepVerifier
                .create(recordDispatchService.dispatch("testCaseId", journalpostDto))
                .expectNext(RecordDispatchResult.failed(
                        "Dokumentobjekt dispatch failed",
                        null
                ))
                .verifyComplete();
    }

}
