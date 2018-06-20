package fi.riista.integration.fcm;

import de.bytefish.fcmjava.client.FcmClient;
import de.bytefish.fcmjava.constants.Constants;
import de.bytefish.fcmjava.http.options.IFcmClientSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FcmConfig {

    @Value("${fcm.api.key}")
    private String fcmApiKey;

    @Bean
    public FcmClient fcmClient() {
        return new FcmClient(new FixedFcmClientSettings(fcmApiKey));
    }

    private static class FixedFcmClientSettings implements IFcmClientSettings {
        private final String apiKey;

        FixedFcmClientSettings(String apiKey) {
            this.apiKey = apiKey;
        }

        @Override
        public String getFcmUrl() {
            return Constants.FCM_URL;
        }

        @Override
        public String getApiKey() {
            return apiKey;
        }
    }
}
