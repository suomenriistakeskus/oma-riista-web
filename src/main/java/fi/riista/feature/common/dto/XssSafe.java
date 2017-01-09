package fi.riista.feature.common.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate DTO fields with this class to indicate that the field
 * (e.g. password) is allowed to contain script elements.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface XssSafe {

    /** The reason for tagging XSS-safety. */
    String value() default "";

}
