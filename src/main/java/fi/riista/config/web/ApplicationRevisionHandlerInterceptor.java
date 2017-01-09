package fi.riista.config.web;

import fi.riista.feature.RuntimeEnvironmentUtil;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

public class ApplicationRevisionHandlerInterceptor extends HandlerInterceptorAdapter {

    private static final long JVM_STARTUP_TIME = System.currentTimeMillis();
    private static final int DELAY_AFTER_BOOT_MINUTES = 5;

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

    private static boolean delayAfterBootExpired() {
        // Apply delay so that revision change is notified to client after both instances have been updated
        return (System.currentTimeMillis() - JVM_STARTUP_TIME) > TimeUnit.MINUTES.toMillis(DELAY_AFTER_BOOT_MINUTES);
    }

    private static boolean shouldAddRevisionHeader(final HttpServletRequest request,
                                                   final HttpServletResponse response) {
        return delayAfterBootExpired() &&
                !response.isCommitted() &&
                request.getRequestURI().startsWith("/api/v1/") &&
                !request.getRequestURI().startsWith("/api/v1/register/") &&
                !request.getRequestURI().startsWith("/api/v1/import/") &&
                !request.getRequestURI().startsWith("/api/v1/export/");
    }
}
