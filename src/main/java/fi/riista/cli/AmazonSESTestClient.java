package fi.riista.cli;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClientBuilder;
import com.google.common.collect.ImmutableMap;
import fi.riista.config.Constants;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailServiceImpl;
import fi.riista.feature.mail.delivery.AmazonMailDeliveryServiceImpl;
import fi.riista.feature.mail.queue.NoopMailProviderImpl;
import fi.riista.feature.mail.delivery.MailDeliveryService;
import fi.riista.feature.mail.queue.OutgoingMailProvider;
import org.springframework.cloud.aws.context.config.annotation.EnableContextCredentials;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;

public class AmazonSESTestClient {

    @Configuration
    @PropertySource("classpath:configuration/aws.properties")
    @EnableContextCredentials(accessKey = "${AWS_ACCESS_KEY_ID:}", secretKey = "${AWS_SECRET_KEY:}")
    static class Context {
        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        public MailDeliveryService<Long> mailDeliveryService(final AWSCredentialsProvider credentialsProvider) {
            return new AmazonMailDeliveryServiceImpl(AmazonSimpleEmailServiceAsyncClientBuilder.standard()
                    .withRegion(Regions.EU_WEST_1)
                    .withCredentials(credentialsProvider)
                    .build());
        }

        @Bean
        public OutgoingMailProvider<Long> outgoingMailProvider() {
            return new NoopMailProviderImpl();
        }

        @Bean
        public MailService mailService() {
            return new MailServiceImpl();
        }
    }

    public static void main(String[] args) {
        try (final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {

            ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("mail", ImmutableMap.of(
                    "mail.enabled", "true",
                    "mail.address.from", "noreply@riista.fi"
            )));
            ctx.getEnvironment().setActiveProfiles(Constants.AMAZON_DATABASE);
            ctx.register(Context.class);
            ctx.refresh();
            ctx.start();

            final MailService bean = ctx.getBean(MailService.class);

            bean.sendImmediate(new MailMessageDTO.Builder()
                    .withFrom("noreply@riista.fi")
                    .withTo("invalid@example.com")
                    .withSubject("Test mail from Amazon SES")
                    .withBody("<html><body><h1>Hello from Amazon</h1>></body></html>"));
        }
    }
}
