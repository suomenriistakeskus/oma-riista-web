package fi.riista.config.web;

import fi.riista.security.UserInfo;
import io.sentry.Sentry;
import io.sentry.context.Context;
import io.sentry.event.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SentryUserContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) throws IOException, ServletException {
        final Context sentryContext = Sentry.getContext();

        if (sentryContext == null) {
            filterChain.doFilter(request, response);
            return;
        }

        final User sentryUser = getSentryUser();

        if (sentryUser != null) {
            sentryContext.setUser(sentryUser);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            sentryContext.clearUser();
        }
    }

    private static User getSentryUser() {
        final SecurityContext securityContext = SecurityContextHolder.getContext();

        if (securityContext != null && securityContext.getAuthentication() != null) {
            final Authentication authentication = securityContext.getAuthentication();

            if (authentication.isAuthenticated()) {
                final UserInfo userInfo = UserInfo.extractFrom(authentication);
                return new User(Long.toString(userInfo.getUserId()), null, null, null);
            }
        }

        return null;
    }
}
