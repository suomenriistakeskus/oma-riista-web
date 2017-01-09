package fi.riista.security.aop;

import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.security.UserInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Set;

/*
 * Custom methods to use in WebSecurityConfig authorization checks.
 */
public class CustomWebSecurityExpressionRoot extends WebSecurityExpressionRoot {
    public CustomWebSecurityExpressionRoot(Authentication authentication, FilterInvocation filterInvocation) {
        super(authentication, filterInvocation);
    }

    public boolean hasPrivilege(SystemUserPrivilege privilege) {
        return UserInfo.extractFrom(authentication).hasPrivilege(privilege);
    }

    /**
     * Check request against use specific whiteList of IP-address patterns.
     *
     * @return true, if at least one pattern matches.
     */
    public boolean matchesWhiteList() {
        if (authentication.isAuthenticated()
                && authentication.getPrincipal() != null
                && authentication.getPrincipal() instanceof UserInfo) {
            final UserInfo userInfo = UserInfo.extractFrom(authentication);

            return getWhiteList(userInfo).stream().anyMatch(entry -> new IpAddressMatcher(entry).matches(request));
        }
        return false;
    }

    private static Set<String> getWhiteList(UserInfo userInfo) {
        return userInfo.getIpWhiteList() != null
                ? StringUtils.commaDelimitedListToSet(userInfo.getIpWhiteList())
                : Collections.<String>emptySet();
    }
}
