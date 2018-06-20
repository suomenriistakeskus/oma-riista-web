package fi.riista.config;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.cache.HighConcurrencyTemplateCache;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.springmvc.SpringTemplateLoader;
import fi.riista.feature.mail.HandlebarsHelperSource;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class HandlebarsConfig {
    @Bean
    public TemplateLoader emailTemplateLoader(ResourceLoader resourceLoader) {
        SpringTemplateLoader templateLoader = new SpringTemplateLoader(resourceLoader);
        templateLoader.setPrefix("classpath:handlebars");
        templateLoader.setSuffix(".hbs");

        return templateLoader;
    }

    @Bean
    public Handlebars emailHandlebars(TemplateLoader templateLoader, MessageSource messageSource) {
        return new Handlebars(templateLoader)
                .with(new HighConcurrencyTemplateCache())
                .registerHelpers(HandlebarsHelperSource.class)
                .registerHelpers(new HandlebarsHelperSource(messageSource));
    }
}
