package fi.riista.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.papertrailapp.logback.Syslog4jAppender;
import org.apache.commons.lang.StringUtils;
import org.productivity.java.syslog4j.impl.net.tcp.ssl.SSLTCPNetSyslogConfig;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:configuration/papertrail.properties")
public class PapertrailConfig {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PapertrailConfig.class);

    private static final String LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} %X{req.remoteHost} %X{req.requestURI} [%thread] %-5level %logger{35}: %m%n%xEx";

    @Value("${papertrail.hostname}")
    private String hostname;

    @Value("${papertrail.port}")
    private Integer port;

    @Value("${papertrail.ident}")
    private String ident;

    public void configureAuditLogAppender() {
        if (StringUtils.isNotBlank(hostname) && StringUtils.isNotBlank(ident) && port != null) {
            LOG.info("External PaperTrail logging enabled, hostname={} port={} ident={}", hostname, port, ident);

            enablePaperTrail(hostname, port, ident);
        } else {
            LOG.info("External PaperTrail logging is disabled");
        }
    }

    private static void enablePaperTrail(final String hostname, final int port, final String ident) {
        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        final PatternLayout patternLayout = new PatternLayout();
        patternLayout.setContext(lc);
        patternLayout.setPattern(LOG_PATTERN);
        patternLayout.start();

        final SSLTCPNetSyslogConfig syslogConfig = new SSLTCPNetSyslogConfig(hostname, port);
        syslogConfig.setIdent(ident);
        syslogConfig.setMaxMessageLength(128000);
        syslogConfig.setSendLocalTimestamp(false);
        syslogConfig.setSendLocalName(false);

        final Syslog4jAppender<ILoggingEvent> syslog4jAppender = new Syslog4jAppender<>();
        syslog4jAppender.setSyslogConfig(syslogConfig);
        syslog4jAppender.setLayout(patternLayout);
        syslog4jAppender.start();

        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        logger.addAppender(syslog4jAppender);
        logger.setLevel(Level.DEBUG);
        logger.setAdditive(true); /* set to true if root should log too */
    }
}
