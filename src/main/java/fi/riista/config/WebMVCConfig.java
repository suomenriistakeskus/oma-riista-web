package fi.riista.config;

import com.google.common.base.Throwables;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.config.web.ApplicationRevisionHandlerInterceptor;
import fi.riista.config.web.CSVMessageConverter;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.error.NotFoundException;
import net.rossillo.spring.web.mvc.CacheControlHandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.CacheControl;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebMvc
@EnableSpringDataWebSupport
@ComponentScan(Constants.API_BASE_PACKAGE)
public class WebMVCConfig extends WebMvcConfigurerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(WebMVCConfig.class);

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Resource
    private CustomJacksonObjectMapper jsonObjectMapper;

    @Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public ViewResolver viewResolver() {
        final InternalResourceViewResolver resolver = new InternalResourceViewResolver();

        resolver.setViewClass(JstlView.class);
        resolver.setPrefix("/WEB-INF/jsp/");
        resolver.setSuffix(".jsp");

        return resolver;
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        /*
         * Ensures that dispatcher servlet can be mapped to '/' and that static resources
         * are still served by the containers default servlet.
         */
        configurer.enable();
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        final CacheControl oneYear = CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic();

        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("/favicon.ico")
                .setCacheControl(oneYear);

        registry.addResourceHandler("/static/**")
                .addResourceLocations("/static/")
                .setCacheControl(oneYear);

        registry.addResourceHandler("/frontend/**")
                .addResourceLocations("/frontend/")
                .setCacheControl(oneYear);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Cache control handler interceptor to assign cache-control headers to HTTP responses.
        final CacheControlHandlerInterceptor cacheControlHandlerInterceptor = new CacheControlHandlerInterceptor();
        cacheControlHandlerInterceptor.setUseExpiresHeader(true);

        registry.addInterceptor(cacheControlHandlerInterceptor);
        registry.addInterceptor(new ApplicationRevisionHandlerInterceptor(runtimeEnvironmentUtil));
    }

    @Bean
    public MappingJackson2HttpMessageConverter jacksonMessageConverter() {
        final MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter();
        jsonMessageConverter.setObjectMapper(jsonObjectMapper);
        return jsonMessageConverter;
    }

    // @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        final MappingJackson2HttpMessageConverter jsonMessageConverter = jacksonMessageConverter();

        final StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        stringHttpMessageConverter.setWriteAcceptCharset(false);

        messageConverters.add(new CSVMessageConverter());
        messageConverters.add(new ByteArrayHttpMessageConverter());
        messageConverters.add(stringHttpMessageConverter);
        messageConverters.add(new ResourceHttpMessageConverter());
        messageConverters.add(new SourceHttpMessageConverter<>());
        messageConverters.add(new AllEncompassingFormHttpMessageConverter());
        messageConverters.add(jsonMessageConverter);
        messageConverters.add(new Jaxb2RootElementHttpMessageConverter());
    }

    // @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        // NOTE: Do not change the order! Only first resolution is used.

        // By default Spring MVC does not log exceptions from @Controller methods.
        if (!runtimeEnvironmentUtil.isIntegrationTestEnvironment()) {
            exceptionResolvers.add(new LoggingHandlerExceptionResolver());
        }
        // Process exception using @ExceptionHandler method inside @Controller
        exceptionResolvers.add(exceptionHandlerExceptionResolver());
        // Return status code specified using @ResponseStatus in exception class
        exceptionResolvers.add(new ResponseStatusExceptionResolver());
        // Send HTTP status code depending on the exception which is mapped to error page in web.xml
        exceptionResolvers.add(new DefaultHandlerExceptionResolver());
    }

    @Bean
    public HandlerExceptionResolver exceptionHandlerExceptionResolver() {
        final ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver = new ExceptionHandlerExceptionResolver();
        exceptionHandlerExceptionResolver.setPreventResponseCaching(true);
        exceptionHandlerExceptionResolver.getMessageConverters().add(jacksonMessageConverter());

        return exceptionHandlerExceptionResolver;
    }

    // Response error message is always generated using RestControllerExceptionAdvice
    private static class LoggingHandlerExceptionResolver implements HandlerExceptionResolver {
        @Override
        public ModelAndView resolveException(HttpServletRequest request,
                                             HttpServletResponse response,
                                             Object handler,
                                             Exception ex) {
            if (ex instanceof NotFoundException) {
                LOG.error("Requested resource was not found for URI={} method={} message={}",
                        request.getRequestURI(), request.getMethod(), ex.getMessage());
                return null;

            } else if (ex instanceof NoSuchRequestHandlingMethodException ||
                    ex instanceof HttpRequestMethodNotSupportedException) {
                LOG.error("Handler method not found for URI={} method={}",
                        request.getRequestURI(), request.getMethod());
                return null;
            }

            final ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);

            if (responseStatus != null) {
                LOG.error("Caught exception with @ResponseStatus {} with message {} for handler {}",
                        ex.getClass().getSimpleName(), ex.getMessage(), getHandlerName(handler));
            } else {
                LOG.error("Handler execution resulted in exception", Throwables.getRootCause(ex));
            }

            return null;
        }

        private static String getHandlerName(Object handler) {
            if (handler != null) {
                if (handler instanceof HandlerMethod) {
                    final HandlerMethod handlerMethod = (HandlerMethod) handler;
                    final Object bean = handlerMethod.getBean();
                    final Method method = handlerMethod.getMethod();

                    return bean.getClass().getName() + "." + method.getName();
                }
            }

            return "?";
        }
    }
}
