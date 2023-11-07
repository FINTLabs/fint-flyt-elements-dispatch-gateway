package no.fintlabs.dispatch.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FailureOperationResult<VALUE> {
    private final VALUE value;
    private final Throwable throwable;
}
