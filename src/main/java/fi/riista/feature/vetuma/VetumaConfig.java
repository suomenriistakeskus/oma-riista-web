package fi.riista.feature.vetuma;

import fi.riista.feature.RuntimeEnvironmentUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Duration;
import org.joda.time.ReadableDuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.Resource;

@Configuration
@PropertySource("classpath:configuration/vetuma.properties")
public class VetumaConfig {

    // Tuotetunnuksella VTJ-VETUMA-Perus pyydetään kaikille VETUMA-asiakkaille tarjolla oleva vakiokyselytuote
    // jossa on seuraavat tiedot: henkilötunnus, nimitiedot, kotikunta, osoitetiedot, äidinkieli, kuolinaika
    // sekä tieto siitä, onko käyttäjä Suomen kansalainen.
    private static final String EXTRA_DATA_VTJ_PERUS = "VTJTT=VTJ-VETUMA-Perus";

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Value("${vetuma.url}")
    private String vetumaLoginUrl;

    @Value("${vetuma.appId}")
    private String clientApplicationId;

    @Value("${vetuma.appName}")
    private String clientApplicationName;

    @Value("${vetuma.ap}")
    private String clientApplicationConfiguration;

    @Value("${vetuma.sharedSecretKey}")
    private String sharedSecret;

    @Value("${vetuma.rcvId}")
    private String sharedSecretIdentifier;

    @Value("${vetuma.responseExpirationSeconds}")
    private int responseExpirationSeconds;

    @Value("${vetuma.extraData}")
    private String extraData;

    @Value("${vetuma.soList}")
    private String allowedAuthenticationMethods;

    @Value("${vetuma.so}")
    private String defaultAuthenticationMethod;

    public String getVetumaLoginUrl() {
        return vetumaLoginUrl;
    }

    public String getVetumaReturnUrl(String path) {
        return runtimeEnvironmentUtil.getBackendBaseUri().resolve(path).toString();
    }

    public ReadableDuration getVetumaTransactionTimeout() {
        return Duration.standardHours(1);
    }

    public String getClientApplicationId() {
        return clientApplicationId;
    }

    public String getClientApplicationName() {
        return clientApplicationName;
    }

    public String getClientApplicationConfiguration() {
        return clientApplicationConfiguration;
    }

    public String getShareSecretKey() {
        return sharedSecret;
    }

    public String getSharedSecretIdentifier() {
        return sharedSecretIdentifier;
    }

    public int getVetumaResponseExpirationSeconds() {
        return responseExpirationSeconds;
    }

    public String getVetumaExtraData() {
        return StringUtils.isNotBlank(extraData) ? extraData : EXTRA_DATA_VTJ_PERUS;
    }

    public String getAllowedAuthenticationMethods() {
        return allowedAuthenticationMethods;
    }

    public String getDefaultAuthenticationMethod() {
        return defaultAuthenticationMethod;
    }
}
