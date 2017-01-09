package fi.riista.security.authorization;

import fi.riista.security.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private static final Logger LOG = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    @Override
    public void handle(final HttpServletRequest request,
                       final HttpServletResponse response,
                       final AccessDeniedException ex) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        final boolean sessionExists = request.getSession(false) != null;

        if (ex instanceof MissingCsrfTokenException) {
            LOG.warn("Missing CSRF token for requestURI={} for user {} with session={} and message: {}",
                    request.getRequestURI(), getActiveUserInfo(), sessionExists, ex.getMessage());

        } else if (ex instanceof CsrfException) {
            LOG.warn("Invalid CSRF token for requestURI={} for user {} with session={} and message: {}",
                    request.getRequestURI(), getActiveUserInfo(), sessionExists, ex.getMessage());

        } else {
            LOG.warn("Access denied for requestURI={} for user {} with exception {} message: {}",
                    request.getRequestURI(), getActiveUserInfo(), ex.getClass().getName(), ex.getMessage());
        }

        if (!response.isCommitted()) {
            response.setContentType("application/json");
            response.getWriter().print("{\"status\": \"FORBIDDEN\"}");
            response.getWriter().flush();
            response.getWriter().close();
        }
    }

    private static String getActiveUserInfo() {
        final SecurityContext context = SecurityContextHolder.getContext();

        if (context == null) {
            return "<no security context>";
        }

        final Authentication authentication = context.getAuthentication();

        if (authentication != null) {
            final Object principal = authentication.getPrincipal();

            if (principal == null) {
                return authentication.getName();
            } else if (authentication.isAuthenticated()) {
                final UserInfo userInfo = UserInfo.extractFrom(authentication);

                return "username=" + userInfo.getUsername() + " userId=" + userInfo.getUserId();
            } else {
                return principal.toString();
            }
        }

        return "<no authentication>";
    }
}
