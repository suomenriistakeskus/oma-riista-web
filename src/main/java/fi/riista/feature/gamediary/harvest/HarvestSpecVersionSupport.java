package fi.riista.feature.gamediary.harvest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface HarvestSpecVersionSupport {
    HarvestSpecVersion since();
    int apiVersion() default 2;
}
