package no.fintlabs.dispatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DispatchMessageFormattingServiceTest {

    DispatchMessageFormattingService dispatchMessageFormattingService;

    @BeforeEach
    public void setup() {
        dispatchMessageFormattingService = new DispatchMessageFormattingService();
    }

    @Test
    public void givenEmptyRefsShouldReturnOptionalEmpty() {
        assertThat(dispatchMessageFormattingService.createFunctionalWarningMessage(
                "testObjectDisplayName",
                "testRefDisplayName",
                List.of()
        )).isEmpty();
    }

    @Test
    public void givenSingleRefShouldFormatMessageForSingleRef() {
        assertThat(dispatchMessageFormattingService.createFunctionalWarningMessage(
                "testObjectDisplayName",
                "testRefDisplayName",
                List.of("ref1")
        )).contains("testObjectDisplayName with testRefDisplayName='ref1'");
    }

    @Test
    public void givenMultipleRefsShouldFormatMessageForMultipleRefs() {
        assertThat(dispatchMessageFormattingService.createFunctionalWarningMessage(
                "testObjectDisplayName",
                "testRefDisplayName",
                List.of("ref1", "ref2", "ref3")
        )).contains("testObjectDisplayNames with testRefDisplayNames=['ref1', 'ref2', 'ref3']");
    }

}
