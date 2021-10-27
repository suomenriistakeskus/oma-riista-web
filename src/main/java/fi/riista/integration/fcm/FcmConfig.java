package fi.riista.integration.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Base64Utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;

@Configuration
public class FcmConfig {

    private static final Logger LOG = LoggerFactory.getLogger(FcmConfig.class);

    @Value("${fcm.db.url}")
    private String fcmDbUrl;

    @Value("${fcm.admin.auth}")
    private String fcmAdminAuth;

    @Bean
    public FcmMulticastSender fcmApplication() throws IOException {

        if (hasText(fcmDbUrl) && hasText(fcmAdminAuth)) {
            LOG.info("Initializing Firebase Cloud messaging");
            final String json = new String(Base64Utils.decodeFromString(fcmAdminAuth));
            final GoogleCredentials credentials =
                    GoogleCredentials.fromStream(IOUtils.toInputStream(json, Charset.forName("UTF-8")));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setDatabaseUrl(fcmDbUrl)
                    .build();

            FirebaseApp.initializeApp(options);

            return new FcmAdminMulticastSender();
        }

        LOG.info("Firebase cloud messaging not configured");
        return new NoopMulticastSender();
    }

    private static class NoopMulticastSender implements FcmMulticastSender{

        @Override
        public Optional<BatchResponse> send(final MulticastMessage message) {
            return Optional.empty();
        }
    }

    private static class FcmAdminMulticastSender implements FcmMulticastSender{

        @Override
        public Optional<BatchResponse> send(final MulticastMessage message) throws FirebaseMessagingException {
            return Optional.of(FirebaseMessaging.getInstance().sendMulticast(message));
        }
    }
}
