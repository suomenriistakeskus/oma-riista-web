package fi.riista.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClientBuilder;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.cache.HighConcurrencyTemplateCache;
import com.github.jknack.handlebars.helper.DefaultHelperRegistry;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.springmvc.SpringTemplateLoader;
import fi.riista.config.profile.AmazonDatabase;
import fi.riista.config.profile.EmbeddedDatabase;
import fi.riista.config.profile.StandardDatabase;
import fi.riista.feature.mail.HandlebarsHelperSource;
import fi.riista.feature.mail.delivery.AmazonMailDeliveryServiceImpl;
import fi.riista.feature.mail.delivery.JavaMailDeliveryServiceImpl;
import fi.riista.feature.mail.delivery.MailDeliveryService;
import fi.riista.feature.mail.delivery.NoopMailDeviceryService;
import fi.riista.feature.mail.queue.DatabaseMailProviderImpl;
import fi.riista.feature.mail.queue.OutgoingMailProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ResourceLoader;

import java.util.Properties;

@Configuration
@PropertySource("classpath:configuration/mail.properties")
public class MailConfig {

    @Bean
    public OutgoingMailProvider<Long> outgoingMailProvider() {
        return new DatabaseMailProviderImpl();
    }

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
                .with(new DefaultHelperRegistry())
                .with(new HighConcurrencyTemplateCache())
                .registerHelpers(HandlebarsHelperSource.class)
                .registerHelpers(new HandlebarsHelperSource(messageSource));
    }

    @Configuration
    @AmazonDatabase
    static class AmazonMailConfiguration {
        @Bean
        public MailDeliveryService<Long> mailDeliveryService(final AWSCredentialsProvider credentialsProvider) {
            return new AmazonMailDeliveryServiceImpl(AmazonSimpleEmailServiceAsyncClientBuilder.standard()
                    .withRegion(Regions.EU_WEST_1)
                    .withCredentials(credentialsProvider)
                    .build());
        }
    }

    @Configuration
    @StandardDatabase
    static class SmtpMailConfiguration {
        @Value("${mail.smtp.host}")
        private String smtpHost;

        @Bean
        public MailDeliveryService<Long> mailDeliveryService() {
            final JavaMailDeliveryServiceImpl sender = new JavaMailDeliveryServiceImpl();
            sender.setJavaMailProperties(getMailProperties());
            sender.setHost(smtpHost);
            return sender;
        }

        private static Properties getMailProperties() {
            final Properties properties = new Properties();
            properties.setProperty("mail.smtp.connectiontimeout", "1000");
            properties.setProperty("mail.smtp.timeout", "5000");
            return properties;
        }
    }

    @Configuration
    @EmbeddedDatabase
    static class TestMailConfiguration {
        @Bean
        public MailDeliveryService<Long> mailDeliveryService() {
            return new NoopMailDeviceryService();
        }
    }
}
