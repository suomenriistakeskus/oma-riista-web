package fi.riista.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClientBuilder;
import fi.riista.config.profile.AmazonDatabase;
import fi.riista.config.profile.EmbeddedDatabase;
import fi.riista.config.profile.StandardDatabase;
import fi.riista.config.properties.MailProperties;
import fi.riista.feature.mail.delivery.AmazonMailDeliveryServiceImpl;
import fi.riista.feature.mail.delivery.JavaMailDeliveryServiceImpl;
import fi.riista.feature.mail.delivery.MailDeliveryService;
import fi.riista.feature.mail.delivery.NoopMailDeliveryService;
import fi.riista.feature.mail.queue.DatabaseMailDeliveryQueue;
import fi.riista.feature.mail.queue.MailDeliveryQueue;
import org.nlab.smtp.pool.SmtpConnectionPool;
import org.nlab.smtp.transport.factory.SmtpConnectionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import javax.mail.Session;

@Configuration
@Import(MailProperties.class)
public class MailConfig {

    @Bean
    public MailDeliveryQueue outgoingMailProvider() {
        return new DatabaseMailDeliveryQueue();
    }

    @Configuration
    @AmazonDatabase
    static class AmazonMailConfiguration {
        @Bean
        public MailDeliveryService mailDeliveryService(final AWSCredentialsProvider credentialsProvider) {
            return new AmazonMailDeliveryServiceImpl(AmazonSimpleEmailServiceAsyncClientBuilder.standard()
                    .withRegion(Regions.EU_WEST_1)
                    .withCredentials(credentialsProvider)
                    .build());
        }
    }

    @Configuration
    @StandardDatabase
    static class SmtpMailConfiguration {
        @Resource
        private MailProperties mailProperties;

        @Bean
        public SmtpConnectionPool smtpConnectionPool() {
            return new SmtpConnectionPool(SmtpConnectionFactoryBuilder.newSmtpBuilder()
                    .session(Session.getInstance(mailProperties.getJavaMailProperties()))
                    .protocol("smtp")
                    .port(25)
                    .host(mailProperties.getSmtpHost())
                    .build());
        }

        @Bean
        public MailDeliveryService mailDeliveryService() {
            return new JavaMailDeliveryServiceImpl(smtpConnectionPool());
        }
    }

    @Configuration
    @EmbeddedDatabase
    static class TestMailConfiguration {
        @Bean
        public MailDeliveryService mailDeliveryService() {
            return new NoopMailDeliveryService();
        }
    }
}
