package fi.riista.servlet;

import ch.qos.logback.classic.helpers.MDCInsertingServletFilter;
import fi.riista.servlet.filter.UncaughtExceptionFilter;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import java.nio.charset.StandardCharsets;

@Order(3)
public class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {
    private static final String NORMAL_CHARACTER_ENCODING = StandardCharsets.UTF_8.name();

    @Override
    protected void beforeSpringSecurityFilterChain(final ServletContext servletContext) {
        // Be sure charsetFilters are registered before spring security, otherwise logins will fail in tomcat with non-ascii passwords
        initCharacterSetFilter(servletContext);
        insertFilters(servletContext,
                new UncaughtExceptionFilter(),
                new MDCInsertingServletFilter(),
                new UrlRewriteFilter());
    }

    private void initCharacterSetFilter(final ServletContext servletContext) {
        final CharacterEncodingFilter normalCharacterEncodingFilter = new CharacterEncodingFilter();
        normalCharacterEncodingFilter.setEncoding(NORMAL_CHARACTER_ENCODING);

        registerFilter(servletContext, "normalCharacterEncodingFilter", normalCharacterEncodingFilter, "/*");
    }

    private void registerFilter(ServletContext servletContext, String name, Filter filter, String pattern) {
        final FilterRegistration.Dynamic filterRegistration = servletContext.addFilter(name, filter);
        filterRegistration.setAsyncSupported(isAsyncSecuritySupported());
        //There might be some filters already registered,
        // make sure charsetFilters are registered before spring security, otherwise logins will fail in tomcat with non-ascii passwords
        final boolean isMatchAfter = false;
        filterRegistration.addMappingForUrlPatterns(getSecurityDispatcherTypes(), isMatchAfter, pattern);
    }
}
