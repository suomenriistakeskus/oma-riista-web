package fi.riista.config;

import fi.riista.MainApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@Import({MainApplicationContext.class, HttpSessionConfig.class})
@PropertySource("classpath:configuration/application.properties")
@ComponentScan({"fi.riista.test", "fi.riista.util"})
public class IntegrationTestApplicationContext {
    static {
        LiquibaseConfig.replaceLiquibaseServiceLocator();
    }

    @Primary
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public Validator mvcValidator() {
        return new LocalValidatorFactoryBean();
    }
}
