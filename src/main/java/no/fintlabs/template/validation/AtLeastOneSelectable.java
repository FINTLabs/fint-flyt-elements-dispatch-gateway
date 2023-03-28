package no.fintlabs.template.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = AtLeastOneSelectableValidator.class)
public @interface AtLeastOneSelectable {

    String message() default "contains no selectables or selectable sources";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
