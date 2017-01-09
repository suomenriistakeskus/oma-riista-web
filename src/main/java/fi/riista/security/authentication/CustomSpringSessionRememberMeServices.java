package fi.riista.security.authentication;

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
import java.util.Optional;
import java.util.Set;

@Component
public class CustomSpringSessionRememberMeServices implements RememberMeServices, LogoutHandler {
    public static final String REMEMBER_ME_LOGIN_ATTR = SpringSessionRememberMeServices.REMEMBER_ME_LOGIN_ATTR;

    private static final String REQUEST_PARAMETER_NAME = "remember-me";

    public static boolean isRememberMeActive(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(REMEMBER_ME_LOGIN_ATTR) != null;
    }

    private static void invalidateSession(HttpServletRequest request) {
        Optional.ofNullable(request.getSession(false)).ifPresent(HttpSession::invalidate);
    }

    @Resource
    private SecurityConfigurationProperties securityConfigurationProperties;

    private Optional<Seconds> getTimeToLive(final Authentication successfulAuthentication) {
        final UserInfo userInfo = UserInfo.extractFrom(successfulAuthentication);
        final Set<String> roles = AuthorityUtils.authorityListToSet(userInfo.getAuthorities());

        if (roles.contains(SystemUser.Role.ROLE_REST.name())) {
            return Optional.empty();
        } else if (roles.contains(SystemUser.Role.ROLE_ADMIN.name()) ||
                roles.contains(SystemUser.Role.ROLE_MODERATOR.name())) {
            return Optional.of(securityConfigurationProperties.getRemeberMeTimeToLiveForModerator());
        } else if (roles.contains(SystemUser.Role.ROLE_USER.name())) {
            return Optional.of(securityConfigurationProperties.getRememberMeTimeToLive());
        }

        return Optional.empty();
    }

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

    @Override
    public void loginSuccess(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Authentication successfulAuthentication) {
        if (BooleanUtils.toBoolean(request.getParameter(REQUEST_PARAMETER_NAME))) {
            getTimeToLive(successfulAuthentication).ifPresent(ttl -> {
                request.setAttribute(REMEMBER_ME_LOGIN_ATTR, true);
                request.getSession().setMaxInactiveInterval(ttl.getSeconds());
            });
        }
    }
}
