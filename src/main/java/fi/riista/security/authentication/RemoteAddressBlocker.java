package fi.riista.security.authentication;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.sso.support.ExternalAuthenticationDetails;
import fi.riista.security.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

/**
 * Match remoteAddress against user specific blocks of white-listed IPv4 addresses.
 * User with ROLE_REST authority will be blocked always if the white-list is empty.
 * NOTE: IPv6 addresses will not work.
 */
public class RemoteAddressBlocker {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteAddressBlocker.class);

    public static void assertNotBlocked(UserDetails userDetails, Authentication authentication) {
        try {
            if (isAllowedRemote(userDetails, authentication)) {
                return;
            }

        } catch (RuntimeException ex) {
            throw new InternalAuthenticationServiceException("Internal error", ex);
        }

        throw new RemoteAddressBlockedException("Remote address is not allowed");
    }

    private static boolean isAllowedRemote(UserDetails userDetails, Authentication authentication) {
        // Extract user specific white-list
        final Set<String> whiteList = getWhiteList(userDetails);

        if (whiteList.isEmpty()) {
            // White-list must be defined for remote API user accounts
            return !hasRole(userDetails, SystemUser.Role.ROLE_REST);
        }

        // Remote address must match at least one entry on the white-list
        return remoteAddressMatchesWhiteList(getRemoteAddress(authentication), whiteList);
    }

    private static boolean remoteAddressMatchesWhiteList(final String remoteAddress, final Set<String> whiteList) {
        for (final String entry : whiteList) {
            if (new IpAddressMatcher(entry).matches(remoteAddress)) {
                LOG.debug("Found matching white-list entry {} for remoteAddress={}", entry, remoteAddress);

                return true;
            }
        }

        LOG.error("Remote address {} is not matched white-list: {}", remoteAddress, whiteList);

        return false;
    }

    private static boolean hasRole(@Nonnull final UserDetails userDetails, @Nonnull final SystemUser.Role role) {
        return AuthorityUtils.authorityListToSet(userDetails.getAuthorities()).contains(role.name());
    }

    @Nonnull
    private static String getRemoteAddress(@Nonnull final Authentication authentication) {
        final Object details = authentication.getDetails();
        if (details != null) {
            if (details instanceof WebAuthenticationDetails) {
                final WebAuthenticationDetails webAuthenticationDetails = (WebAuthenticationDetails) details;
                return webAuthenticationDetails.getRemoteAddress();

            } else if (details instanceof ExternalAuthenticationDetails) {
                final ExternalAuthenticationDetails externalAuthenticationDetails =
                    (ExternalAuthenticationDetails) details;
                return externalAuthenticationDetails.getRemoteAddress();
            }
        }
        throw new InternalAuthenticationServiceException("Authentication details is missing");
    }

    @Nonnull
    private static Set<String> getWhiteList(@Nonnull final UserDetails userDetails) {
        final UserInfo userInfo = UserInfo.extractFrom(userDetails);
        final String ipWhiteList = userInfo.getIpWhiteList();

        if (ipWhiteList == null) {
            return Collections.emptySet();
        }

        return StringUtils.commaDelimitedListToSet(ipWhiteList);
    }

    private RemoteAddressBlocker() {
        throw new AssertionError();
    }
}
