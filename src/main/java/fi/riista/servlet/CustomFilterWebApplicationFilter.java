package fi.riista.servlet;

import ch.qos.logback.classic.helpers.MDCInsertingServletFilter;
import fi.riista.servlet.filter.UncaughtExceptionFilter;
import org.springframework.core.Conventions;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.support.MultipartFilter;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.REQUEST;

@Order(-100)
public class CustomFilterWebApplicationFilter implements WebApplicationInitializer {

    private static final EnumSet<DispatcherType> DISPATCHER_TYPES = EnumSet.of(REQUEST, ASYNC);

    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
        final Filter[] filterArray = new Filter[]{
                new MDCInsertingServletFilter(),
                // Catch all remaining unhandled exceptions to prevent default Tomcat error page
                new UncaughtExceptionFilter(),
                // Be sure characterEncodingFilters are registered before Spring Security,
                // otherwise logins will fail in tomcat with non-ascii passwords
                new CharacterEncodingFilter(StandardCharsets.UTF_8.name()),
                // Rewrite versioned asset locations
                new UrlRewriteFilter(),
                // Parse multipart requests before accessing session or database
                new MultipartFilter()
        };

        for (final Filter filter : filterArray) {
            insertFilter(servletContext, filter);
        }
    }

    private static void insertFilter(final ServletContext servletContext, final Filter filter) {
        final String filterName = Conventions.getVariableName(filter);
        final FilterRegistration.Dynamic registration = servletContext.addFilter(filterName, filter);
        registration.addMappingForUrlPatterns(DISPATCHER_TYPES, false, "/*");
        registration.setAsyncSupported(true);
    }
}
