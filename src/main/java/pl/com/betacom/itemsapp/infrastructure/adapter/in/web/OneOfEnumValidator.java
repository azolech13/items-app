package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

public class OneOfEnumValidator implements ConstraintValidator<OneOfEnum, String> {

    private Set<String> accepted;
    private String acceptedList;

    @Override
    public void initialize(OneOfEnum annotation) {
        accepted = Arrays.stream(annotation.value().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());
        acceptedList = Arrays.stream(annotation.value().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || accepted.contains(value)) {
            return true;
        }
        context.unwrap(HibernateConstraintValidatorContext.class)
                .addMessageParameter("accepted", acceptedList);
        return false;
    }
}
