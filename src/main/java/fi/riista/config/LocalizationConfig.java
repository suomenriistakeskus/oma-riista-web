package fi.riista.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;

@Configuration
public class LocalizationConfig {

    @Bean(name = "messageSource")
    public MessageSource messageSource() {
        // MessageSource configuration for localized messages.
        final ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();

        source.setBasenames("classpath:/i18n/messages", "classpath:ValidationMessages");
        source.setUseCodeAsDefaultMessage(true);
        source.setFallbackToSystemLocale(false);
        source.setDefaultEncoding(StandardCharsets.ISO_8859_1.name());

        return source;
    }

}
