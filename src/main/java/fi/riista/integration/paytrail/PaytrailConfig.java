package fi.riista.integration.paytrail;

import fi.riista.integration.paytrail.auth.PaytrailAuthCodeDigest;
import fi.riista.integration.paytrail.auth.PaytrailAuthCodeVerifier;
import fi.riista.integration.paytrail.rest.PaytrailRestAdapter;
import fi.riista.integration.paytrail.rest.client.PaytrailApiCredentials;
import fi.riista.integration.paytrail.rest.client.PaytrailRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Configuration
@PropertySource("classpath:configuration/paytrail.properties")
public class PaytrailConfig {

    private static final Logger LOG = LoggerFactory.getLogger(PaytrailConfig.class);

    @Value("${paytrail.merchantId}")
    private String merchantId;

    @Value("${paytrail.merchantSecret}")
    private String merchantSecret;

    @Bean
    public PaytrailApiCredentials paytrailApiCredentials() {
        return StringUtils.hasText(merchantId) && StringUtils.hasText(merchantSecret)
                ? new PaytrailApiCredentials(merchantId, merchantSecret)
                : demoCredentials();
    }

    private static PaytrailApiCredentials demoCredentials() {
        LOG.warn("Paytrail credentials not available, using demo credentials");
        return new PaytrailApiCredentials("13466", "6pKF4jkv97zmqBJ3ZL8gUw5DfT2NMQ");
    }

    @Bean
    public PaytrailAuthCodeVerifier paytrailChecksumVerifier() {
        return new PaytrailAuthCodeVerifier(paytrailApiCredentials(),
                PaytrailAuthCodeDigest.SHA256, Duration.ofHours(2));
    }

    @Bean
    public PaytrailRestAdapter paytrailApi(final @Qualifier("paytrailMarshaller") Jaxb2Marshaller marshaller,
                                           final ClientHttpRequestFactory clientHttpRequestFactory) {
        return new PaytrailRestAdapter(new PaytrailRestTemplate(
                clientHttpRequestFactory, marshaller, paytrailApiCredentials()));
    }
}
