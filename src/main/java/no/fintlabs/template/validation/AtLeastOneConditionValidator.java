package no.fintlabs.template.validation;

import no.fintlabs.template.model.ValuePredicate;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

public class AtLeastOneConditionValidator implements HibernateConstraintValidator<AtLeastOneCondition, ValuePredicate> {

    @Override
    public boolean isValid(ValuePredicate value, HibernateConstraintValidatorContext hibernateConstraintValidatorContext) {
        return value.getDefined() != null
                || value.getValue() != null
                || value.getNotValue() != null;
    }

}
