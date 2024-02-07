package no.fintlabs.flyt.gateway.application.archive.template.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = UniqueKeysValidator.class)
public @interface UniqueKeys {

    String DUPLICATE_KEYS_REF = "duplicateKeys";

    String message() default "contains duplicate keys: {" + DUPLICATE_KEYS_REF + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
