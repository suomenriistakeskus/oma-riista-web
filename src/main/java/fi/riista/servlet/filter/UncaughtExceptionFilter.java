package fi.riista.servlet.filter;

import io.sentry.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.util.NestedServletException;

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
            return;

        } catch (MultipartException me) {
            LOG.error("{}: Multipart upload failed: {}", me.getClass().getSimpleName(), me.getMessage());

        } catch (NestedServletException nse) {
            // Should be handled automatically by HandlerExceptionResolver
            LOG.error("Caught nested servlet exception", nse.getCause());
            Sentry.capture(nse.getCause());

        } catch (Throwable th) {
            LOG.error("Uncaught exception", th);
            Sentry.capture(th);
        }

        if (response.isCommitted()) {
            LOG.warn("Response is committed, could not send HTTP error response.");
            return;
        }

        try (final PrintWriter writer = response.getWriter()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            writer.print("{\"status\": \"ERROR\", \"message\": \"uncaught exception\"}");
            writer.flush();

        } catch (IOException ignore) {
        }
    }
}
