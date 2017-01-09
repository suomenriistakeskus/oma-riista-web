package fi.riista.security.otp;

import fi.riista.config.properties.SecurityConfigurationProperties;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.security.authentication.RemoteAddressBlocker;
import fi.riista.util.F;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.Set;

public class OneTimePasswordAuthenticationProvider extends DaoAuthenticationProvider {

    @Resource
    private SecurityConfigurationProperties securityConfigurationProperties;

    @Resource
    private OneTimePasswordCodeService oneTimePasswordCodeService;

    // For uni-testing
    public void setSecurityConfigurationProperties(final SecurityConfigurationProperties securityConfigurationProperties) {
        this.securityConfigurationProperties = securityConfigurationProperties;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) {
        super.additionalAuthenticationChecks(userDetails, authentication);

        checkOneTimePassword(userDetails, authentication);

        if (!securityConfigurationProperties.isDisableAddressBlocker()) {
            RemoteAddressBlocker.assertNotBlocked(userDetails, authentication);
        }
    }

    private void checkOneTimePassword(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) {
        if (hasAdminOrModeratorRoleAndLoginOtpEnabled(userDetails) || isEnforcedAndExtAuthOtpEnabled(authentication)) {
            if (authentication instanceof OneTimePasswordAuthenticationToken) {
                oneTimePasswordCodeService.checkOneTimePassword(
                        userDetails, (OneTimePasswordAuthenticationToken) authentication);

            } else {
                throw new BadCredentialsException("One time password is required");
            }
        }
    }

    private boolean isEnforcedAndExtAuthOtpEnabled(final UsernamePasswordAuthenticationToken authentication) {
        if (securityConfigurationProperties.isExtAuthOtpEnabled() &&
                authentication instanceof OneTimePasswordAuthenticationToken) {
            final OneTimePasswordAuthenticationToken oneTimePasswordAuthenticationToken =
                    (OneTimePasswordAuthenticationToken) authentication;

            return oneTimePasswordAuthenticationToken.isEnforceOneTimePassword() ||
                    oneTimePasswordAuthenticationToken.hasOneTimePassword();

        }
        return false;
    }

    private boolean hasAdminOrModeratorRoleAndLoginOtpEnabled(final UserDetails userDetails) {
        return securityConfigurationProperties.isLoginOtpEnabled() && hasAdminOrModeratorRole(userDetails);
    }

    private static boolean hasAdminOrModeratorRole(final UserDetails userDetails) {
        final Set<String> roleNames = AuthorityUtils.authorityListToSet(userDetails.getAuthorities());
        return F.containsAny(roleNames, SystemUser.Role.ROLE_ADMIN.name(), SystemUser.Role.ROLE_MODERATOR.name());
    }
}
