package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = OneOfEnumValidator.class)
@Target({
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.RECORD_COMPONENT,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface OneOfEnum {

    Class<? extends Enum<?>> value();

    String message() default "accepts only {accepted}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
