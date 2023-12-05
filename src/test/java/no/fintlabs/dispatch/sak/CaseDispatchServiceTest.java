package no.fintlabs.dispatch.sak;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.dispatch.sak.result.CaseDispatchResult;
import no.fintlabs.mapping.SakMappingService;
import no.fintlabs.model.instance.ArchiveInstance;
import no.fintlabs.model.instance.CaseSearchParametersDto;
import no.fintlabs.model.instance.SakDto;
import no.fintlabs.web.archive.CaseSearchParametersService;
import no.fintlabs.web.archive.FintArchiveClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseDispatchServiceTest {

    @Mock
    private SakMappingService sakMappingService;
    @Mock
    private CaseSearchParametersService caseSearchParametersService;
    @Mock
    private FintArchiveClient fintArchiveClient;
    @InjectMocks
    private CaseDispatchService caseDispatchService;

    @Test
    public void givenAcceptedCaseShouldReturnAcceptedResultWithCaseId() {
        SakDto sakDto = mock(SakDto.class);
        SakResource sakResource = mock(SakResource.class);
        doReturn(sakResource).when(sakMappingService).toSakResource(sakDto);

        SakResource sakResourceResult = mock(SakResource.class);
        Identifikator identifikator = mock(Identifikator.class);
        doReturn(identifikator).when(sakResourceResult).getMappeId();
        doReturn("testArchiveCaseId").when(identifikator).getIdentifikatorverdi();
        doReturn(Mono.just(sakResourceResult)).when(fintArchiveClient).postCase(sakResource);

        StepVerifier.create(
                        caseDispatchService.dispatch(sakDto)
                )
                .expectNext(CaseDispatchResult.accepted("testArchiveCaseId"))
                .verifyComplete();

        verify(sakMappingService, times(1)).toSakResource(sakDto);
        verifyNoMoreInteractions(sakMappingService);

        verify(fintArchiveClient, times(1)).postCase(sakResource);
        verifyNoMoreInteractions(fintArchiveClient);
    }

    @Test
    public void givenWebclientResponseExceptionFromPostCaseShouldReturnDeclinedResultWithErrorMessage() {
        SakDto sakDto = mock(SakDto.class);
        SakResource sakResource = mock(SakResource.class);
        doReturn(sakResource).when(sakMappingService).toSakResource(sakDto);

        WebClientResponseException webClientResponseException = mock(WebClientResponseException.class);
        doReturn("test response body").when(webClientResponseException).getResponseBodyAsString();
        doReturn(Mono.error(webClientResponseException)).when(fintArchiveClient).postCase(sakResource);

        StepVerifier.create(
                        caseDispatchService.dispatch(sakDto)
                )
                .expectNext(CaseDispatchResult.declined("test response body"))
                .verifyComplete();

        verify(sakMappingService, times(1)).toSakResource(sakDto);
        verifyNoMoreInteractions(sakMappingService);

        verify(fintArchiveClient, times(1)).postCase(sakResource);
        verifyNoMoreInteractions(fintArchiveClient);
    }

    @Test
    public void givenExceptionOtherThanWebclientResponseExceptionFromPostCaseShouldReturnFailedResult() {
        SakDto sakDto = mock(SakDto.class);
        SakResource sakResource = mock(SakResource.class);
        doReturn(sakResource).when(sakMappingService).toSakResource(sakDto);
        doReturn(Mono.error(new RuntimeException())).when(fintArchiveClient).postCase(sakResource);

        StepVerifier.create(
                        caseDispatchService.dispatch(sakDto)
                )
                .expectNext(CaseDispatchResult.failed())
                .verifyComplete();

        verify(sakMappingService, times(1)).toSakResource(sakDto);
        verifyNoMoreInteractions(sakMappingService);

        verify(fintArchiveClient, times(1)).postCase(sakResource);
        verifyNoMoreInteractions(fintArchiveClient);
    }

    @Test
    public void findCasesBySearchShouldCreateFilterWithCaseAndSearchParametersAndCallFindCasesWithFilter() {
        ArchiveInstance archiveInstance = mock(ArchiveInstance.class);

        SakDto sakDto = mock(SakDto.class);
        doReturn(sakDto).when(archiveInstance).getNewCase();

        CaseSearchParametersDto caseSearchParametersDto = mock(CaseSearchParametersDto.class);
        doReturn(caseSearchParametersDto).when(archiveInstance).getCaseSearchParameters();

        doReturn("test case filter").when(caseSearchParametersService).createFilterQueryParamValue(
                sakDto,
                caseSearchParametersDto
        );

        SakResource sakResource = mock(SakResource.class);
        doReturn(Mono.just(List.of(sakResource))).when(fintArchiveClient).findCasesWithFilter("test case filter");

        StepVerifier.create(
                        caseDispatchService.findCasesBySearch(archiveInstance)
                )
                .expectNext(List.of(sakResource))
                .verifyComplete();

        verify(caseSearchParametersService,times(1)).createFilterQueryParamValue(sakDto, caseSearchParametersDto);
        verifyNoMoreInteractions(caseSearchParametersService);

        verify(fintArchiveClient,times(1)).findCasesWithFilter("test case filter");
        verifyNoMoreInteractions(fintArchiveClient);
    }

}