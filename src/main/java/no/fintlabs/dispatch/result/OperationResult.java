package no.fintlabs.dispatch.result;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import reactor.core.publisher.Mono;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OperationResult<VALUE, DECLINED_VALUE, ERROR_VALUE> {
    private final Mono<VALUE> success;
    private final Mono<DeclinedOperationResult<DECLINED_VALUE>> declined;
    private final Mono<FailureOperationResult<ERROR_VALUE>> error;
}
