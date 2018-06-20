package fi.riista.validation;

import fi.riista.feature.account.registration.VetumaTransaction;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

@Pattern(regexp = VetumaTransaction.TRID_PATTERN_STRING)
@ReportAsSingleViolation
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, METHOD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Constraint(validatedBy = {})
@HasLengthConstrainedByValidator
@XssSafe
public @interface VetumaTransactionId {
    String message() default "{fi.riista.validation.VetumaTransactionId.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
