package fi.riista.config;

import fi.riista.feature.RuntimeEnvironmentUtil;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Configuration
public class LocalizationConfig {

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Bean(name = "messageSource")
    public MessageSource messageSource() {
        // MessageSource configuration for localized messages.
        final ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();

        source.setBasenames("classpath:/i18n/messages", "classpath:ValidationMessages");
        source.setUseCodeAsDefaultMessage(true);
        source.setFallbackToSystemLocale(false);
        source.setDefaultEncoding(StandardCharsets.ISO_8859_1.name());

        if (runtimeEnvironmentUtil.isDevelopmentEnvironment()) {
            // Check for updates on every refresh, otherwise cache forever
            source.setCacheSeconds(1);
        }

        return source;
    }

    @Bean
    public LocaleResolver localeResolver() {
        final SessionLocaleResolver localeResolver = new SessionLocaleResolver();

        // This should force to use browser language when not available
        localeResolver.setDefaultLocale(runtimeEnvironmentUtil.getDefaultLocale());

        return localeResolver;
    }

}
