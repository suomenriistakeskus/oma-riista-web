package fi.riista.config.profile;

import fi.riista.config.Constants;
import org.springframework.context.annotation.Profile;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Profile(Constants.AMAZON_DATABASE)
public @interface AmazonDatabase {

}
