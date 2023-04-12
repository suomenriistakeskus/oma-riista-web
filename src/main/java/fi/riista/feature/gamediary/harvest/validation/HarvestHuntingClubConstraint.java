package fi.riista.feature.gamediary.harvest.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Constraint(validatedBy = HarvestHuntingClubValidator.class)
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HarvestHuntingClubConstraint {
    String message() default "HuntingClub must be null for mooselike harvest";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

