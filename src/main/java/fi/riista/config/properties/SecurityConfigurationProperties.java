package fi.riista.config.properties;

import fi.riista.util.DateUtil;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Value;

public class SecurityConfigurationProperties {
    private static final Period TTL_REMEMBER_ME_NORMAL = Period.days(30);
    private static final Period TTL_REMEMBER_ME_MODERATOR = Period.hours(3);

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.otp.global.secret}")
    private String otpGlobalSalt;

    @Value("${security.login.otp.enabled}")
    private boolean otpLoginEnabled;

    @Value("${security.extauth.otp.enabled}")
    private boolean otpExtAuthEnabled;


    // Allow disable for unit-testing
    private boolean disableAddressBlocker;

    public byte[] getJwtSecret() {
        return jwtSecret.getBytes();
    }

    public Seconds getRememberMeTimeToLive() {
        return TTL_REMEMBER_ME_NORMAL.toDurationFrom(DateUtil.now()).toStandardSeconds();
    }

    public Seconds getRememberMeTimeToLiveForModerator() {
        return TTL_REMEMBER_ME_MODERATOR.toDurationFrom(DateUtil.now()).toStandardSeconds();
    }

    public String getOtpGlobalSalt() {
        return otpGlobalSalt;
    }

    public void setOtpGlobalSalt(final String otpGlobalSalt) {
        this.otpGlobalSalt = otpGlobalSalt;
    }

    public boolean isLoginOtpEnabled() {
        return otpLoginEnabled;
    }

    public void setOtpLoginEnabled(boolean otpLoginEnabled) {
        this.otpLoginEnabled = otpLoginEnabled;
    }

    public boolean isDisableAddressBlocker() {
        return disableAddressBlocker;
    }

    public void setDisableAddressBlocker(boolean disableAddressBlocker) {
        this.disableAddressBlocker = disableAddressBlocker;
    }

    public boolean isExtAuthOtpEnabled() {
        return otpExtAuthEnabled;
    }

    public void setOtpExtAuthEnabled(boolean otpExtAuthEnabled) {
        this.otpExtAuthEnabled = otpExtAuthEnabled;
    }
}
