package fi.riista.servlet;

import org.springframework.core.annotation.Order;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

@Order(100)
public class SessionWebApplicationInitializer extends AbstractHttpSessionApplicationInitializer {
    @Override
    protected String getDispatcherWebApplicationContextSuffix() {
        return AbstractDispatcherServletInitializer.DEFAULT_SERVLET_NAME;
    }
}
