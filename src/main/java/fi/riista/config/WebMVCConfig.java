package fi.riista.config;

import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.config.web.ApplicationRevisionHandlerInterceptor;
import fi.riista.config.web.CSVMessageConverter;
import fi.riista.config.web.ControllerExceptionAdvice;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.util.Locales;
import net.rossillo.spring.web.mvc.CacheControlHandlerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebMvc
@EnableSpringDataWebSupport
@ComponentScan(Constants.API_BASE_PACKAGE)
public class WebMVCConfig implements WebMvcConfigurer {

    // Limit multipart request size to 100 MiB
    private static final long MAX_UPLOAD_SIZE = 100 * 1024 * 1024;

    // Limit upload file size to 50 MiB
    private static final long MAX_UPLOAD_SIZE_PER_FILE = 50 * 1024 * 1024;

    // Write uploads to disks when larger than 1 MiB
    private static final int MAX_UPLOAD_IN_MEMORY_SIZE = 1024 * 1024;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Resource
    private CustomJacksonObjectMapper jsonObjectMapper;

    @Bean(name = MultipartFilter.DEFAULT_MULTIPART_RESOLVER_BEAN_NAME)
    public MultipartResolver multipartResolver() {
        //return new StandardServletMultipartResolver();
        final CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setMaxUploadSizePerFile(MAX_UPLOAD_SIZE_PER_FILE);
        resolver.setMaxUploadSize(MAX_UPLOAD_SIZE);
        resolver.setMaxInMemorySize(MAX_UPLOAD_IN_MEMORY_SIZE);
        resolver.setDefaultEncoding(Constants.DEFAULT_ENCODING);
        return resolver;
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
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/api/v1/anon/**")
                .allowedMethods("GET")
                .allowCredentials(false)
                .allowedOrigins("https://riista.fi");
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        final CacheControl oneYear = CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic();

        registry.addResourceHandler("/static/elainlajikuvat/**")
                .addResourceLocations("/static/elainlajikuvat/")
                .setCacheControl(oneYear);

        registry.addResourceHandler("/static/badges/**")
                .addResourceLocations("/static/badges/")
                .setCacheControl(oneYear);

        registry.addResourceHandler("/static/**")
                .addResourceLocations("/static/")
                .setCacheControl(CacheControl.noStore());

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

        // Process exception using @ExceptionHandler method inside @Controller
        exceptionResolvers.add(exceptionHandlerExceptionResolver());
    }

    @Bean
    public HandlerExceptionResolver exceptionHandlerExceptionResolver() {
        final ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver = new ExceptionHandlerExceptionResolver();
        exceptionHandlerExceptionResolver.setPreventResponseCaching(true);
        exceptionHandlerExceptionResolver.getMessageConverters().add(jacksonMessageConverter());

        return exceptionHandlerExceptionResolver;
    }

    @Bean
    public ControllerExceptionAdvice globalControllerExceptionAdvice() {
        return new ControllerExceptionAdvice();
    }

    @Bean
    public LocaleResolver localeResolver() {
        final SessionLocaleResolver localeResolver = new SessionLocaleResolver();

        // This should force to use browser language when not available
        localeResolver.setDefaultLocale(Locales.FI);

        return localeResolver;
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
