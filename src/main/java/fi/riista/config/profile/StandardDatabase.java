package fi.riista.config.profile;

import fi.riista.config.Constants;

import org.springframework.context.annotation.Profile;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Profile(Constants.STANDARD_DATABASE)
public @interface StandardDatabase {

}
