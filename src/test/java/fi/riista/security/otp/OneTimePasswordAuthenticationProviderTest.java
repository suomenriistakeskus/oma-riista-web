package fi.riista.security.otp;

import fi.riista.config.properties.SecurityConfigurationProperties;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Clock;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.util.Assert.notNull;

public class OneTimePasswordAuthenticationProviderTest {

    private static final String USER = "user";
    private static final String ADMIN = "admin";
    private static final String MODERATOR = "moderator";

    @Test
    public void testSupportsOneTimePasswordToken() {
        OneTimePasswordAuthenticationProvider provider = createProvider();
        assertTrue(provider.supports(OneTimePasswordAuthenticationToken.class));
    }

    @Test
    public void testDoesNotSupportInvalidTokens() {
        OneTimePasswordAuthenticationProvider provider = createProvider();
        assertFalse(provider.supports(PreAuthenticatedAuthenticationToken.class));
    }

    @Test(expected = AuthenticationServiceException.class)
    public void testThrowExceptionOnInvalidToken() {
        OneTimePasswordAuthenticationProvider provider = createProvider();
        provider.authenticate(new UsernamePasswordAuthenticationToken("", ""));
    }

    @Test(expected = BadCredentialsException.class)
    public void testIncorrectPasswordForRoleUser() {
        OneTimePasswordAuthenticationProvider provider = createProvider();
        provider.authenticate(new OneTimePasswordAuthenticationToken(USER, "wrong", null));
    }

    @Test
    public void testCorrectPasswordAcceptedForRoleUser() {
        OneTimePasswordAuthenticationProvider provider = createProvider();
        Authentication authentication = provider.authenticate(new OneTimePasswordAuthenticationToken(USER, USER, null));
        assertTrue(authentication.isAuthenticated());
    }

    @Test(expected = OneTimePasswordRequiredException.class)
    public void testMissingOtpForRoleAdminFails() {
        OneTimePasswordAuthenticationProvider provider = createProvider();
        provider.authenticate(new OneTimePasswordAuthenticationToken(ADMIN, ADMIN, null));
    }

    @Test(expected = OneTimePasswordRequiredException.class)
    public void testMissingOtpForRoleModeratorFails() {
        OneTimePasswordAuthenticationProvider provider = createProvider();
        provider.authenticate(new OneTimePasswordAuthenticationToken(MODERATOR, MODERATOR, null));
    }

    @Test
    public void testOtpEnabledOnlyToExternalAuthAdminOk() {
        OneTimePasswordAuthenticationProvider provider = createProvider(false, true);
        provider.authenticate(new OneTimePasswordAuthenticationToken(ADMIN, ADMIN, null));
    }

    @Test
    public void testOtpEnabledOnlyToExternalAuthModeratorOk() {
        OneTimePasswordAuthenticationProvider provider = createProvider(false, true);
        provider.authenticate(new OneTimePasswordAuthenticationToken(MODERATOR, MODERATOR, null));
    }

    @Test(expected = BadCredentialsException.class)
    public void testIncorrectOtpForRoleAdminFails() {
        OneTimePasswordAuthenticationProvider provider = createProvider();
        provider.authenticate(new OneTimePasswordAuthenticationToken(ADMIN, ADMIN, "1234"));
    }

    @Test(expected = BadCredentialsException.class)
    public void testIncorrectOtpForRoleModeratorFails() {
        OneTimePasswordAuthenticationProvider provider = createProvider();
        provider.authenticate(new OneTimePasswordAuthenticationToken(MODERATOR, MODERATOR, "1234"));
    }

    @Test
    public void testCorrectOtpForRoleAdminAccepted() {
        final User user = new User(ADMIN, ADMIN, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

        OneTimePasswordAuthenticationProvider provider = createProvider();
        Authentication authentication = provider.authenticate(new OneTimePasswordAuthenticationToken(ADMIN, ADMIN, getCorrectCode(user)));
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    public void testCorrectOtpForRoleAdminAcceptedWithTolerance() {
        OneTimePasswordAuthenticationProvider provider = createProvider();

        final User user = new User(ADMIN, ADMIN, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
        final String correctCode = getCorrectCode(user, -1);

        Authentication authentication = provider.authenticate(new OneTimePasswordAuthenticationToken(ADMIN, ADMIN, correctCode));
        assertTrue(authentication.isAuthenticated());
    }

    @Test(expected = BadCredentialsException.class)
    public void testCodeNotAcceptedAfterTooLong() {
        OneTimePasswordAuthenticationProvider provider = createProvider();

        final User user = new User(ADMIN, ADMIN, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
        final String correctCode = getCorrectCode(user, 1);

        provider.authenticate(new OneTimePasswordAuthenticationToken(ADMIN, ADMIN, correctCode));
    }

    @Test(expected = BadCredentialsException.class)
    public void testCodeNotAcceptedAfterTooEarly() {
        OneTimePasswordAuthenticationProvider provider = createProvider();

        final User user = new User(ADMIN, ADMIN, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
        final String correctCode = getCorrectCode(user, -2);

        provider.authenticate(new OneTimePasswordAuthenticationToken(ADMIN, ADMIN, correctCode));
    }

    @Test
    public void testCorrectOtpForRoleModeratorAccepted() {
        final OneTimePasswordAuthenticationProvider provider = createProvider();

        final User user = new User(MODERATOR, MODERATOR, AuthorityUtils.createAuthorityList("ROLE_MODERATOR"));

        Authentication authentication = provider.authenticate(new OneTimePasswordAuthenticationToken(MODERATOR, MODERATOR, getCorrectCode(user)));
        assertTrue(authentication.isAuthenticated());
    }

    @Test(expected = BadCredentialsException.class)
    public void testDifferentCodeRequiredForDifferentUser() {
        OneTimePasswordAuthenticationProvider provider = createProvider();

        final User user1 = new User(ADMIN, ADMIN, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

        provider.authenticate(new OneTimePasswordAuthenticationToken(ADMIN, ADMIN, getCorrectCode(user1)));
        provider.authenticate(new OneTimePasswordAuthenticationToken(MODERATOR, MODERATOR, getCorrectCode(user1)));
    }

    @Test
    public void testOtpExceptionContainsCorrectCode() {
        OneTimePasswordAuthenticationProvider provider = createProvider();

        try {
            provider.authenticate(new OneTimePasswordAuthenticationToken(ADMIN, ADMIN, null));

        } catch (OneTimePasswordRequiredException ex) {
            notNull(ex.getUserDetails());
            assertEquals(ex.getUserDetails().getUsername(), ADMIN);

            final User user = new User(ADMIN, ADMIN, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
            assertEquals(getCorrectCode(user), ex.getExpectedCode());
        }
    }

    @Test(expected = OneTimePasswordRequiredException.class)
    public void testOneTimePasswordRequirementEnforcedForLogin() {
        OneTimePasswordAuthenticationProvider provider = createProvider(true, true);
        doTestOtpRequired(provider);
    }

    @Test(expected = OneTimePasswordRequiredException.class)
    public void testOneTimePasswordRequirementEnforcedForExternalAuth() {
        OneTimePasswordAuthenticationProvider provider = createProvider(false, true);
        doTestOtpRequired(provider);
    }

    @Test
    public void testOneTimePasswordNotRequiredWhenNotEnabled() {
        OneTimePasswordAuthenticationProvider provider = createProvider(false, false);
        doTestOtpRequired(provider);
    }

    private static void doTestOtpRequired(OneTimePasswordAuthenticationProvider provider) {
        OneTimePasswordAuthenticationToken tokenWithoutOtp = new OneTimePasswordAuthenticationToken(USER, USER, null);
        tokenWithoutOtp.enforceOneTimePassword();

        Authentication authentication = provider.authenticate(tokenWithoutOtp);
        assertTrue(authentication.isAuthenticated());
    }

    private static String getCorrectCode(User user) {
        return getCorrectCode(user, 0);
    }

    private static String getCorrectCode(User user, int offsetInterval) {
        final OneTimePasswordCodeService codeService = new OneTimePasswordCodeService(createMockProperties());
        final String otpSecret = codeService.getOtpSecret(user);
        return new Totp(otpSecret, new TestOtpClock(offsetInterval)).now();
    }

    private static class TestOtpClock extends Clock {
        private final int offsetInterval;

        private TestOtpClock(final int offsetInterval) {
            this.offsetInterval = offsetInterval;
        }

        @Override
        public long getCurrentInterval() {
            return super.getCurrentInterval() + offsetInterval;
        }
    }

    private static OneTimePasswordAuthenticationProvider createProvider() {
        return createProvider(true, false);
    }

    private static OneTimePasswordAuthenticationProvider createProvider(boolean loginOtp, boolean extAuthOtp) {
        SecurityConfigurationProperties properties = createMockProperties(loginOtp, extAuthOtp);
        UserDetailsService userDetailsService = Mockito.mock(UserDetailsService.class);

        Mockito.when(userDetailsService.loadUserByUsername(ArgumentMatchers.eq(USER))).thenReturn(
                new User(USER, USER, AuthorityUtils.createAuthorityList("ROLE_USER")));

        Mockito.when(userDetailsService.loadUserByUsername(ArgumentMatchers.eq(ADMIN))).thenReturn(
                new User(ADMIN, ADMIN, AuthorityUtils.createAuthorityList("ROLE_ADMIN")));

        Mockito.when(userDetailsService.loadUserByUsername(ArgumentMatchers.eq(MODERATOR))).thenReturn(
                new User(MODERATOR, MODERATOR, AuthorityUtils.createAuthorityList("ROLE_MODERATOR")));

        OneTimePasswordAuthenticationProvider provider = new OneTimePasswordAuthenticationProvider();
        provider.setSecurityConfigurationProperties(properties);
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(new PlaintextPasswordEncoder());

        OneTimePasswordCodeService codeService = new OneTimePasswordCodeService(properties);
        ReflectionTestUtils.setField(provider, "oneTimePasswordCodeService", codeService);

        return provider;
    }

    private static SecurityConfigurationProperties createMockProperties() {
        return createMockProperties(true, false);
    }

    private static SecurityConfigurationProperties createMockProperties(boolean loginOtp, boolean extAuthOtp) {
        SecurityConfigurationProperties props = new SecurityConfigurationProperties();
        props.setDisableAddressBlocker(true);
        props.setOtpGlobalSalt("WOZWFXF5UVU6S2EP");
        props.setOtpLoginEnabled(loginOtp);
        props.setOtpExtAuthEnabled(extAuthOtp);
        return props;
    }
}
