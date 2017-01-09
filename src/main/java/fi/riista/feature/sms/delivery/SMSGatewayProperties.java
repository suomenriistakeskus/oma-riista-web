package fi.riista.feature.sms.delivery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:configuration/sms.properties")
public class SMSGatewayProperties {
    @Value("${sms.gateway.uri}")
    private String gatewayUri;

    @Value("${sms.gateway.username}")
    private String gatewayUsername;

    @Value("${sms.gateway.password}")
    private String gatewayPassword;

    // Maximum length allowed is 11 characters for names,
    // 16 characters for national phone numbers,
    // and 15 characters for international phone numbers
    @Value("${sms.gateway.source.name}")
    private String gatewaySourceName;

    @Value("${sms.gateway.quota.period.minutes}")
    private long quotaPeriodMinutes;

    @Value("${sms.gateway.quota.size}")
    private int quotaSize;

    public String getGatewayUri() {
        return gatewayUri;
    }

    public String getGatewayUsername() {
        return gatewayUsername;
    }

    public String getGatewayPassword() {
        return gatewayPassword;
    }

    public String getGatewaySourceName() {
        return gatewaySourceName;
    }

    public long getQuotaPeriodMinutes() {
        return quotaPeriodMinutes;
    }

    public int getQuotaSize() {
        return quotaSize;
    }
}
