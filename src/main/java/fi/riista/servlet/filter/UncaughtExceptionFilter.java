package fi.riista.servlet.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class UncaughtExceptionFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(UncaughtExceptionFilter.class);

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException {
        try {
            filterChain.doFilter(request, response);
        } catch (Throwable e) {
            LOG.error("Uncaught exception", e);

            try (final PrintWriter writer = response.getWriter()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.print("");
                writer.flush();
                writer.close();
            } catch (IOException ignore) {
            }
        }
    }
}
