package fi.riista.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Pattern(regexp = "\\d{14}")
@ReportAsSingleViolation
@Retention(RetentionPolicy.RUNTIME)
@Target({ FIELD, METHOD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Constraint(validatedBy={})
@HasLengthConstrainedByValidator
@XssSafe
public @interface FinnishPropertyIdentifier {
    String message() default "{fi.riista.validation.FinnishPropertyIdentifier.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
