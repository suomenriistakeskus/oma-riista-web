package fi.riista.config.web;

import fi.riista.feature.RuntimeEnvironmentUtil;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ApplicationRevisionHandlerInterceptor extends HandlerInterceptorAdapter {

    private final RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    public ApplicationRevisionHandlerInterceptor(final RuntimeEnvironmentUtil runtimeEnvironmentUtil) {
        this.runtimeEnvironmentUtil = runtimeEnvironmentUtil;
    }

    @Override
    public final boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler) {
        if (shouldAddRevisionHeader(request, response)) {
            response.setHeader("X-Revision", runtimeEnvironmentUtil.getRevision());
        }

        return true;
    }

    private static boolean shouldAddRevisionHeader(final HttpServletRequest request,
                                                   final HttpServletResponse response) {
        return !response.isCommitted() &&
                request.getRequestURI().startsWith("/api/v1/") &&
                !request.getRequestURI().startsWith("/api/v1/register/") &&
                !request.getRequestURI().startsWith("/api/v1/import/") &&
                !request.getRequestURI().startsWith("/api/v1/export/");
    }
}
