package no.fintlabs.dispatch.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeclinedOperationResult<VALUE> {
    private final VALUE value;
    private final String message;
}
