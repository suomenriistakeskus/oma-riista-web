package fi.riista.util.jpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * In JPA, @ManyToOne and @OneToOne associations are fetched eagerly by default.
 * Because of possible performance issues stemming from that, the default
 * behavior is overridden in such (non-inverse) associations by a lazy fetch
 * policy which is enforced by a JUnit test. In rare situations, where the
 * developer sees eager fetch fitting better, those specific associations can be
 * annotated with this annotation to have the JUnit test pass.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface EagerFetch {

    /** The reason for enabling eager fetching. */
    String value() default "";

}
