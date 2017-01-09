package fi.riista.config.quartz;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public @interface QuartzScheduledJob {
    String enabledProperty() default "";
    String cronExpression() default "";
    long fixedRate() default -1;
    String name() default "";
    String group() default "";
}
