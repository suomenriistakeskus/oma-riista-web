package fi.riista.servlet.filter;

import com.newrelic.api.agent.NewRelic;
import io.sentry.Sentry;
import io.sentry.SentryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class UncaughtExceptionFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(UncaughtExceptionFilter.class);

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) {
        try {
            filterChain.doFilter(request, response);

        } catch (Throwable e) {
            LOG.error("Uncaught exception", e);

            final SentryClient sentry = Sentry.getStoredClient();

            if (sentry != null) {
                sentry.sendException(e);
            }

            NewRelic.noticeError(e, false);

            if (response.isCommitted()) {
                LOG.warn("Response is committed, could not send HTTP error response.");
                return;
            }

            try (final PrintWriter writer = response.getWriter()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.print("error");
                writer.flush();
                writer.close();
            } catch (IOException ignore) {
            }
        }
    }
}
