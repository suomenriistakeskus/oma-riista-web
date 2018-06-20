package fi.riista.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;

/**
 * Annotating a String field with this class indicates that the field (e.g.
 * password) is allowed to contain script elements or its content is considered
 * safe in some other way. This annotation may also be used as a
 * meta-annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ANNOTATION_TYPE, FIELD})
public @interface XssSafe {

    /** The reason for tagging XSS-safety. */
    String value() default "";

}
