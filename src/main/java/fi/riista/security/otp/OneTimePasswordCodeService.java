package fi.riista.security.otp;

import fi.riista.config.properties.SecurityConfigurationProperties;
import fi.riista.feature.account.user.SystemUser;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class OneTimePasswordCodeService {
    private SecurityConfigurationProperties securityConfigurationProperties;

    @Autowired
    public OneTimePasswordCodeService(SecurityConfigurationProperties securityConfigurationProperties) {
        this.securityConfigurationProperties = securityConfigurationProperties;
    }

    public static String getOtpSecret(final String username, final String salt) {
        // User specific secret is combined with global secret as salt to generate time-based OTP codes
        // Only first 10 bytes is used, so SHA1 (10 bytes) is plenty
        final byte[] keyMaterial = DigestUtils.sha1(salt + username);
        final byte[] secretKey = Arrays.copyOf(keyMaterial, Math.max(10, keyMaterial.length));
        return new String(new Base32().encode(secretKey));
    }

    public String getOtpSecret(final SystemUser user) {
        return getOtpSecret(user.getUsername(), securityConfigurationProperties.getOtpGlobalSalt());
    }

    public String getOtpSecret(final UserDetails userDetails) {
        return getOtpSecret(userDetails.getUsername(), securityConfigurationProperties.getOtpGlobalSalt());
    }

    public static boolean checkOneTimePassword(final String otpSecret, final String oneTimeToken) {
        return StringUtils.isNumeric(oneTimeToken) && new Totp(otpSecret).verify(oneTimeToken);
    }

    public void checkOneTimePassword(final UserDetails userDetails, final OneTimePasswordAuthenticationToken token) {
        final String otpSecret = getOtpSecret(userDetails);

        if (token.getOneTimePassword() == null) {
            getExpectedOneTimePassword(userDetails, otpSecret);
            return;
        }

        if (!checkOneTimePassword(otpSecret, token.getOneTimePassword().toString())) {
            // Code was provided but was incorrect
            throw new BadCredentialsException("One time password check failed");
        }
    }

    private static void getExpectedOneTimePassword(final UserDetails userDetails, final String otpSecret) {
        // Code was not provided by user in authentication.
        final String expectedCode = new Totp(otpSecret).now();

        throw new OneTimePasswordRequiredException(userDetails, expectedCode);
    }
}
