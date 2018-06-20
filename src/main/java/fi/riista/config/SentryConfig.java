package fi.riista.config;

import io.sentry.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import java.net.URI;

@Configuration("sentryConfig")
@PropertySource("classpath:configuration/sentry.properties")
public class SentryConfig {
    private static final Logger LOG = LoggerFactory.getLogger(SentryConfig.class);

    @Value("${sentry.dsn.private}")
    private URI sentryDsnPrivate;

    @Value("${sentry.dsn.public}")
    private URI sentryDsnPublic;

    @PostConstruct
    public void init() {
        if (sentryDsnPrivate != null && sentryDsnPrivate.getHost() != null) {
            LOG.info("Using Sentry DSN {}", sentryDsnPrivate);
            Sentry.init(sentryDsnPrivate.toString());
        } else {
            LOG.info("Sentry DSN not configured");
            Sentry.init("noop://localhost?async=false");
        }
    }

    public URI getSentryDsnPrivate() {
        return sentryDsnPrivate;
    }

    public URI getSentryDsnPublic() {
        return sentryDsnPublic;
    }
}
