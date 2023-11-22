package no.fintlabs.dispatch.file;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.DokumentfilResource;
import no.fintlabs.dispatch.DispatchMessageFormattingService;
import no.fintlabs.web.archive.FintArchiveClient;
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
class FilesWarningMessageServiceTest {

    @Mock
    FintArchiveClient fintArchiveClient;

    @Mock
    DispatchMessageFormattingService dispatchMessageFormattingService;

    @InjectMocks
    FilesWarningMessageService filesWarningMessageService;

    @Test
    public void givenEmptyFileLinkListCreateFunctionalWarningMessageShouldReturnOptionalEmpty() {
        StepVerifier.create(
                        filesWarningMessageService.createFunctionalWarningMessage(List.of())
                )
                .expectNextMatches(Optional::isEmpty)
                .verifyComplete();
    }

    @Test
    public void givenFileLinksAndSuccessfulResponseFromGetFileShouldCallFormattingServiceWithIds() {
        Link link = mock(Link.class);

        DokumentfilResource dokumentfilResource = mock(DokumentfilResource.class);
        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi("123");
        doReturn(identifikator).when(dokumentfilResource).getSystemId();
        doReturn(Mono.just(dokumentfilResource)).when(fintArchiveClient).getFile(link);

        StepVerifier.create(filesWarningMessageService.createFunctionalWarningMessage(List.of(link)))
                .expectNext(Optional.empty())
                .verifyComplete();

        verify(fintArchiveClient, times(1)).getFile(link);
        verifyNoMoreInteractions(fintArchiveClient);

        verify(dispatchMessageFormattingService, times(1)).createFunctionalWarningMessage(
                "dokumentobjekt", "id", List.of("123")
        );
        verifyNoMoreInteractions(dispatchMessageFormattingService);
    }

    @Test
    public void givenFileLinksAndErrorResponseFromGetFileShouldCallFormattingServiceWithLinks() {
        Link link = mock(Link.class);

        doReturn("https://www.testlink.com").when(link).getHref();
        doReturn(Mono.error(new RuntimeException())).when(fintArchiveClient).getFile(link);

        StepVerifier.create(filesWarningMessageService.createFunctionalWarningMessage(List.of(link)))
                .expectNext(Optional.empty())
                .verifyComplete();

        verify(fintArchiveClient, times(1)).getFile(link);
        verifyNoMoreInteractions(fintArchiveClient);

        verify(dispatchMessageFormattingService, times(1)).createFunctionalWarningMessage(
                "dokumentobjekt", "link", List.of("https://www.testlink.com")
        );
        verifyNoMoreInteractions(dispatchMessageFormattingService);
    }

    @Test
    public void givenEmptyReturnFromFormattingServiceShouldReturnEmpty() {
        Link link = mock(Link.class);

        doReturn("https://www.testlink.com").when(link).getHref();
        doReturn(Mono.error(new RuntimeException())).when(fintArchiveClient).getFile(link);

        doReturn(Optional.empty())
                .when(dispatchMessageFormattingService).createFunctionalWarningMessage(
                        "dokumentobjekt", "link", List.of("https://www.testlink.com")
                );

        StepVerifier.create(
                        filesWarningMessageService.createFunctionalWarningMessage(List.of(
                                link
                        ))
                )
                .expectNext(Optional.empty())
                .verifyComplete();

        verify(fintArchiveClient, times(1)).getFile(link);
        verifyNoMoreInteractions(fintArchiveClient);

        verify(dispatchMessageFormattingService, times(1)).createFunctionalWarningMessage(
                "dokumentobjekt", "link", List.of("https://www.testlink.com")
        );
        verifyNoMoreInteractions(dispatchMessageFormattingService);
    }

    @Test
    public void givenValueReturnFromFormattingServiceShouldReturnValue() {
        Link link = mock(Link.class);

        doReturn("https://www.testlink.com").when(link).getHref();
        doReturn(Mono.error(new RuntimeException())).when(fintArchiveClient).getFile(link);

        doReturn(Optional.of("formatted message"))
                .when(dispatchMessageFormattingService).createFunctionalWarningMessage(
                        "dokumentobjekt", "link", List.of("https://www.testlink.com")
                );

        StepVerifier.create(
                        filesWarningMessageService.createFunctionalWarningMessage(List.of(
                                link
                        ))
                )
                .expectNext(Optional.of("formatted message"))
                .verifyComplete();

        verify(fintArchiveClient, times(1)).getFile(link);
        verifyNoMoreInteractions(fintArchiveClient);

        verify(dispatchMessageFormattingService, times(1)).createFunctionalWarningMessage(
                "dokumentobjekt", "link", List.of("https://www.testlink.com")
        );
        verifyNoMoreInteractions(dispatchMessageFormattingService);
    }

}