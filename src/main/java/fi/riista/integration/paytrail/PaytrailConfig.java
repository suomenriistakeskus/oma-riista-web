package fi.riista.integration.paytrail;

import fi.riista.integration.paytrail.auth.PaytrailCredentials;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:configuration/paytrail.properties")
public class PaytrailConfig {

    @Value("${paytrail.rk.merchantId}")
    private String rkMerchantId;

    @Value("${paytrail.rk.merchantSecret}")
    private String rkMerchantSecret;

    @Value("${paytrail.mmm.merchantId}")
    private String mmmMerchantId;

    @Value("${paytrail.mmm.merchantSecret}")
    private String mmmMerchantSecret;

    @Qualifier("rk")
    @Bean
    public PaytrailCredentials rkCredentials() {
        return new PaytrailCredentials(rkMerchantId, rkMerchantSecret);
    }

    @Qualifier("mmm")
    @Bean
    public PaytrailCredentials mmmCredentials() {
        return new PaytrailCredentials(mmmMerchantId, mmmMerchantSecret);
    }
}
