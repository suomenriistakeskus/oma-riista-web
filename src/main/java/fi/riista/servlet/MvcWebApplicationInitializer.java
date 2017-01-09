package fi.riista.servlet;

import fi.riista.config.HttpSessionConfig;
import fi.riista.config.WebMVCConfig;
import fi.riista.config.WebSecurityConfig;
import fi.riista.config.properties.WebAppContextInitializer;
import fi.riista.MainApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

@Order(1)
public class MvcWebApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    // Limit upload size to 50 MBytes
    private static final long MAX_UPLOAD_SIZE = 50 * 1024 * 1024;

    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
        servletContext.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        super.onStartup(servletContext);
    }

    @Override
    protected FrameworkServlet createDispatcherServlet(final WebApplicationContext servletAppContext) {
        final DispatcherServlet dispatcherServlet = new DispatcherServlet(servletAppContext);
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
        return dispatcherServlet;
    }

    @Override
    protected void customizeRegistration(final ServletRegistration.Dynamic registration) {
        registration.setMultipartConfig(new MultipartConfigElement("", MAX_UPLOAD_SIZE, -1, 0));
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{
                MainApplicationContext.class,
                HttpSessionConfig.class,
                WebSecurityConfig.class,
                WebMVCConfig.class
        };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected ApplicationContextInitializer<?>[] getRootApplicationContextInitializers() {
        return new ApplicationContextInitializer<?>[]{
                new WebAppContextInitializer()
        };
    }
}
