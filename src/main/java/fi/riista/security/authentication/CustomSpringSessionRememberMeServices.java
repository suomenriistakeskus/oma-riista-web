package fi.riista.security.authentication;

import fi.riista.config.HttpSessionConfig;
import fi.riista.config.properties.SecurityConfigurationProperties;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.security.UserInfo;
import org.apache.commons.lang.BooleanUtils;
import org.joda.time.Seconds;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

@Component
public class CustomSpringSessionRememberMeServices implements RememberMeServices, LogoutHandler {
    public static final String REMEMBER_ME_LOGIN_ATTR = SpringSessionRememberMeServices.REMEMBER_ME_LOGIN_ATTR;

    private static final String REQUEST_PARAMETER_NAME = "remember-me";

    public static boolean isRememberMeActive(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);

        return session != null && session.getMaxInactiveInterval() >
                HttpSessionConfig.DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS;
    }

    @Resource
    private SecurityConfigurationProperties securityConfigurationProperties;

    @Override
    public Authentication autoLogin(final HttpServletRequest request,
                                    final HttpServletResponse response) {
        return null;
    }

    @Override
    public void loginFail(final HttpServletRequest request,
                          final HttpServletResponse response) {
        invalidateSession(request);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        invalidateSession(request);
    }

    private static void invalidateSession(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }

    @Override
    public void loginSuccess(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Authentication successfulAuthentication) {
        if (!BooleanUtils.toBoolean(request.getParameter(REQUEST_PARAMETER_NAME))) {
            return;
        }

        final Seconds maxInactiveInterval = getRememberMeIfPossible(successfulAuthentication);

        if (maxInactiveInterval == null) {
            return;
        }

        // Control cookie type created on login by DefaultCookieSerializer using request attribute
        // Cookie with maximum life time is created for remember me.
        // Cookie with session life time is created otherwise.
        request.setAttribute(REMEMBER_ME_LOGIN_ATTR, true);

        // Extend normal 30 minute session timeout
        request.getSession().setMaxInactiveInterval(maxInactiveInterval.getSeconds());
    }

    private Seconds getRememberMeIfPossible(final Authentication successfulAuthentication) {
        final SystemUser.Role role = getRole(successfulAuthentication).orElse(null);

        if (role == null) {
            return null;
        }

        if (role.isModeratorOrAdmin()) {
            return securityConfigurationProperties.getRememberMeTimeToLiveForModerator();
        } else if (role.isNormalUser()) {
            return securityConfigurationProperties.getRememberMeTimeToLive();
        }

        return null;
    }

    private static Optional<SystemUser.Role> getRole(final Authentication successfulAuthentication) {
        final UserInfo userInfo = UserInfo.extractFrom(successfulAuthentication);
        final Set<String> roles = AuthorityUtils.authorityListToSet(userInfo.getAuthorities());

        return Arrays.stream(SystemUser.Role.values())
                .filter(r -> roles.contains(r.name()))
                .findAny();
    }
}
