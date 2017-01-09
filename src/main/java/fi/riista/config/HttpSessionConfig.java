package fi.riista.config;

import fi.riista.security.authentication.CustomSpringSessionRememberMeServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.MapSession;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;
import org.springframework.session.jdbc.JdbcOperationsSessionRepository;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableScheduling
public class HttpSessionConfig extends SpringHttpSessionConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(HttpSessionConfig.class);

    @Bean
    public CookieSerializer cookieSerializer() {
        // Use standard Servlet cookie name, because it has been hard-coded in WP mobile client
        final DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("JSESSIONID");
        serializer.setRememberMeRequestAttribute(CustomSpringSessionRememberMeServices.REMEMBER_ME_LOGIN_ATTR);
        return serializer;
    }

    @Bean
    public JdbcOperationsSessionRepository sessionRepository(
            DataSource dataSource, PlatformTransactionManager transactionManager) {
        final JdbcOperationsSessionRepository sessionRepository = new JdbcOperationsSessionRepository(
                dataSource, transactionManager);
        sessionRepository.setDefaultMaxInactiveInterval(MapSession.DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS);
        sessionRepository.setConversionService(safeConversionService());
        return sessionRepository;
    }

    private static GenericConversionService safeConversionService() {
        final GenericConversionService converter = new GenericConversionService();
        converter.addConverter(Object.class, byte[].class, new SerializingConverter());

        final DeserializingConverter byteConverter = new DeserializingConverter();
        converter.addConverter(byte[].class, Object.class, (byte[] bytes) -> {
            try {
                return byteConverter.convert(bytes);
            } catch (SerializationFailedException e) {
                LOG.error("Could not extract attribute: {}", e.getMessage());
                return null;
            }
        });

        return converter;
    }
}
