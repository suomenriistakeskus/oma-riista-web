package fi.riista.integration.paytrail;

import fi.riista.integration.paytrail.auth.PaytrailAuthService;
import fi.riista.integration.paytrail.auth.PaytrailCredentials;
import fi.riista.integration.paytrail.util.PaytrailMessageSignatureUtil;
import fi.riista.integration.paytrail.util.PaytrailMessageSignatureUtilImpl;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Bean
    public PaytrailAuthService paytrailAuthService() {
        return new PaytrailAuthService(new PaytrailCredentials(rkMerchantId, rkMerchantSecret),
                new PaytrailCredentials(mmmMerchantId, mmmMerchantSecret));
    }

    @Bean
    public PaytrailMessageSignatureUtil paytrailMessageSignatureUtil(@Autowired PaytrailAuthService authService) {
        return new PaytrailMessageSignatureUtilImpl(authService);
    }
}
