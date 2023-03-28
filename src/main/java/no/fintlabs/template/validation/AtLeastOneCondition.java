package no.fintlabs.template.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = AtLeastOneConditionValidator.class)
public @interface AtLeastOneCondition {

    String message() default "contains no predicate conditions";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
