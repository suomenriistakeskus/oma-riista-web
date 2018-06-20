package fi.riista.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

@Constraint(validatedBy = FinnishCreditorReferenceValidator.class)
@Target({ANNOTATION_TYPE, FIELD, METHOD})
@Retention(RetentionPolicy.RUNTIME)
@HasLengthConstrainedByValidator
@XssSafe
public @interface FinnishCreditorReference {
    String message() default "{fi.riista.validation.FinnishCreditorReference.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean verifyChecksum() default true;
}
