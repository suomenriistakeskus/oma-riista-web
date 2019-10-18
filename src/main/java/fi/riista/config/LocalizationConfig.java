package fi.riista.config;

import fi.riista.util.Locales;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

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

    @Bean
    public LocaleResolver localeResolver() {
        final SessionLocaleResolver localeResolver = new SessionLocaleResolver();

        // This should force to use browser language when not available
        localeResolver.setDefaultLocale(Locales.FI);

        return localeResolver;
    }

}
