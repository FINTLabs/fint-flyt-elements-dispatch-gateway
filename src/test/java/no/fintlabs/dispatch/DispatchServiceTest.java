package no.fintlabs.dispatch;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.dispatch.journalpost.RecordsDispatchService;
import no.fintlabs.dispatch.journalpost.result.RecordsDispatchResult;
import no.fintlabs.dispatch.sak.CaseDispatchService;
import no.fintlabs.dispatch.sak.result.CaseDispatchResult;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.model.CaseDispatchType;
import no.fintlabs.model.instance.ArchiveInstance;
import no.fintlabs.model.instance.JournalpostDto;
import no.fintlabs.model.instance.SakDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispatchServiceTest {

    @Mock
    private CaseDispatchService caseDispatchService;
    @Mock
    private RecordsDispatchService recordsDispatchService;
    @InjectMocks
    private DispatchService dispatchService;
    @Mock
    private InstanceFlowHeaders instanceFlowHeaders;
    @Mock
    private ArchiveInstance archiveInstance;

    @Test
    public void givenArchiveInstanceWithTypeNewShouldReturnAcceptedResultWithCaseId() {
        doReturn(CaseDispatchType.NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();
        doReturn(Mono.just(CaseDispatchResult.accepted("testCaseId"))).when(caseDispatchService).dispatch(sakDto);

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.accepted("testCaseId"))
                .verifyComplete();

        verify(caseDispatchService, times(1)).dispatch(sakDto);
        verifyNoMoreInteractions(caseDispatchService);
    }

    @Test
    public void givenArchiveInstanceWithTypeByIdWithJournalpostShouldReturnAcceptedWithArchiveCaseIdAndJournalpostId() {
        doReturn(CaseDispatchType.BY_ID).when(archiveInstance).getType();
        doReturn("testCaseId").when(archiveInstance).getCaseId();

        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(List.of(journalpostDto)).when(archiveInstance).getJournalpost();

        RecordsDispatchResult recordsDispatchResult = mock(RecordsDispatchResult.class);
        doReturn(Mono.just(recordsDispatchResult)).when(recordsDispatchService).dispatch("testCaseId", List.of(journalpostDto));
        doReturn(DispatchStatus.ACCEPTED).when(recordsDispatchResult).getStatus();
        doReturn(List.of(1L)).when(recordsDispatchResult).getJournalpostIds();

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.accepted("testCaseId-[1]"))
                .verifyComplete();

        verify(recordsDispatchService, times(1)).dispatch("testCaseId", List.of(journalpostDto));
        verifyNoMoreInteractions(recordsDispatchResult);
    }

    @Test
    public void givenDeclinedDispatchArchiveInstanceWithTypeByIdWithJournalpostWithDispatchStatusDeclinedShouldReturnDeclinedWithErrorMessage() {
        doReturn(CaseDispatchType.BY_ID).when(archiveInstance).getType();
        doReturn("testCaseId").when(archiveInstance).getCaseId();

        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(List.of(journalpostDto)).when(archiveInstance).getJournalpost();

        RecordsDispatchResult recordsDispatchResult = mock(RecordsDispatchResult.class);
        doReturn(Mono.just(recordsDispatchResult)).when(recordsDispatchService).dispatch("testCaseId", List.of(journalpostDto));
        doReturn(DispatchStatus.DECLINED).when(recordsDispatchResult).getStatus();
        doReturn(List.of("test warning message")).when(recordsDispatchResult).getFunctionalWarningMessages();
        doReturn("test error message").when(recordsDispatchResult).getErrorMessage();

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.declined("Journalpost was declined by the destination. " +
                        "!!!Already successfully dispatched test warning message!!! Error message from destination: 'test error message'"
                ))
                .verifyComplete();

        verify(recordsDispatchService, times(1)).dispatch("testCaseId", List.of(journalpostDto));
        verifyNoMoreInteractions(recordsDispatchService);
    }

    @Test
    public void givenArchiveInstanceWithTypeBySearchOrNewWithMissingJournalPostShouldReturnDeclined() {
        doReturn(CaseDispatchType.BY_SEARCH_OR_NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();
        doReturn(Optional.of(List.of())).when(sakDto).getJournalpost();

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.declined("Instance contains no records"))
                .verifyComplete();
    }

    @Test
    public void givenArchiveInstanceWithTypeBySearchOrNewWithJournalPostShouldReturnDeclinedWhenMultipleCasesAreFound() {
        doReturn(CaseDispatchType.BY_SEARCH_OR_NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(Optional.of(List.of(journalpostDto))).when(sakDto).getJournalpost();

        SakResource sakResource1 = mock(SakResource.class);
        SakResource sakResource2 = mock(SakResource.class);
        doReturn(Mono.just(List.of(sakResource1, sakResource2))).when(caseDispatchService).findCasesBySearch(archiveInstance);

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.declined("Found multiple cases"))
                .verifyComplete();

        verify(caseDispatchService, times(1)).findCasesBySearch(archiveInstance);
        verifyNoMoreInteractions(caseDispatchService);
    }

    @Test
    public void givenArchiveInstanceWithTypeBySearchOrNewWithOneJournalPostShouldReturnAcceptedWithArchiveCaseIdAndJournalpostIdWhenOneCaseIsFound() {
        doReturn(CaseDispatchType.BY_SEARCH_OR_NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        JournalpostDto journalpostDto = mock(JournalpostDto.class);
        doReturn(Optional.of(List.of(journalpostDto))).when(sakDto).getJournalpost();

        SakResource sakResource = mock(SakResource.class);
        doReturn(Mono.just(List.of(sakResource))).when(caseDispatchService).findCasesBySearch(archiveInstance);
        Identifikator identifikator = mock(Identifikator.class);
        doReturn(identifikator).when(sakResource).getMappeId();
        doReturn("testCaseId").when(identifikator).getIdentifikatorverdi();

        RecordsDispatchResult recordsDispatchResult = mock(RecordsDispatchResult.class);
        doReturn(Mono.just(recordsDispatchResult)).when(recordsDispatchService).dispatch("testCaseId", List.of(journalpostDto));
        doReturn(DispatchStatus.ACCEPTED).when(recordsDispatchResult).getStatus();

        doReturn(List.of(1L)).when(recordsDispatchResult).getJournalpostIds();

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.accepted("testCaseId-[1]"))
                .verifyComplete();

        verify(caseDispatchService, times(1)).findCasesBySearch(archiveInstance);
        verifyNoMoreInteractions(caseDispatchService);

        verify(recordsDispatchService, times(1)).dispatch("testCaseId", List.of(journalpostDto));
        verifyNoMoreInteractions(recordsDispatchResult);
    }

    @Test
    public void givenArchiveInstanceWithTypeBySearchOrNewWithMultipleJournalPostsShouldReturnAcceptedWithArchiveCaseIdAndJournalpostIdsWhenOneCaseIsFound() {
        doReturn(CaseDispatchType.BY_SEARCH_OR_NEW).when(archiveInstance).getType();

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        JournalpostDto journalpostDto1 = mock(JournalpostDto.class);
        JournalpostDto journalpostDto2 = mock(JournalpostDto.class);
        doReturn(Optional.of(List.of(journalpostDto1, journalpostDto2))).when(sakDto).getJournalpost();

        SakResource sakResource = mock(SakResource.class);
        doReturn(Mono.just(List.of(sakResource))).when(caseDispatchService).findCasesBySearch(archiveInstance);
        Identifikator identifikator = mock(Identifikator.class);
        doReturn(identifikator).when(sakResource).getMappeId();
        doReturn("testCaseId").when(identifikator).getIdentifikatorverdi();

        RecordsDispatchResult recordsDispatchResult = mock(RecordsDispatchResult.class);
        doReturn(Mono.just(recordsDispatchResult)).when(recordsDispatchService).dispatch("testCaseId", List.of(journalpostDto1, journalpostDto2));
        doReturn(DispatchStatus.ACCEPTED).when(recordsDispatchResult).getStatus();

        doReturn(List.of(1L, 2L)).when(recordsDispatchResult).getJournalpostIds();

        StepVerifier.create(
                        dispatchService.process(instanceFlowHeaders, archiveInstance)
                )
                .expectNext(DispatchResult.accepted("testCaseId-[1,2]"))
                .verifyComplete();

        verify(caseDispatchService, times(1)).findCasesBySearch(archiveInstance);
        verifyNoMoreInteractions(caseDispatchService);
    }

}