package fi.riista.config;

import fi.riista.feature.RuntimeEnvironmentUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:configuration/application.properties")
@Import({LocalizationConfig.class})
public class MailServiceTestContext {
    @Bean
    public RuntimeEnvironmentUtil runtimeEnvironmentUtil() {
        return new RuntimeEnvironmentUtil();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
