package fi.riista.security.otp;

import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;

import java.util.Collections;
import java.util.Objects;

public class OneTimePasswordFilterConfigurer implements SecurityConfigurer<DefaultSecurityFilterChain, HttpSecurity> {

    private final OneTimePasswordAuthenticationFilter authFilter;
    private AuthenticationEntryPoint authenticationEntryPoint;

    public OneTimePasswordFilterConfigurer(final String loginProcessingUrl,
                                           AuthenticationSuccessHandler successHandler,
                                           AuthenticationFailureHandler failureHandler,
                                           AuthenticationEntryPoint entryPoint) {
        this.authFilter = new OneTimePasswordAuthenticationFilter(loginProcessingUrl);
        this.authFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(loginProcessingUrl, "POST"));
        this.authFilter.setAuthenticationSuccessHandler(successHandler);
        this.authFilter.setAuthenticationFailureHandler(failureHandler);
        this.authFilter.setAllowSessionCreation(true);
        this.authenticationEntryPoint = entryPoint;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(HttpSecurity http) {
        ExceptionHandlingConfigurer<?> exceptionHandling = http.getConfigurer(ExceptionHandlingConfigurer.class);

        if(exceptionHandling != null) {
            ContentNegotiationStrategy contentNegotiationStrategy = http.getSharedObject(ContentNegotiationStrategy.class);
            if(contentNegotiationStrategy == null) {
                contentNegotiationStrategy = new HeaderContentNegotiationStrategy();
            }
            MediaTypeRequestMatcher preferredMatcher = new MediaTypeRequestMatcher(contentNegotiationStrategy,
                    MediaType.APPLICATION_XHTML_XML, new MediaType("image","*"), MediaType.TEXT_HTML, MediaType.TEXT_PLAIN);
            preferredMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
            exceptionHandling.defaultAuthenticationEntryPointFor(authenticationEntryPoint, preferredMatcher);
        }
    }

    @Override
    public void configure(HttpSecurity http) {
        authFilter.setAuthenticationDetailsSource(new WebAuthenticationDetailsSource());
        authFilter.setApplicationEventPublisher(
                Objects.requireNonNull(http.getSharedObject(ApplicationContext.class)));
        authFilter.setAuthenticationManager(
                Objects.requireNonNull(http.getSharedObject(AuthenticationManager.class)));
        authFilter.setSessionAuthenticationStrategy(
                Objects.requireNonNull(http.getSharedObject(SessionAuthenticationStrategy.class)));
        authFilter.setRememberMeServices(
                Objects.requireNonNull(http.getSharedObject(RememberMeServices.class)));

        http.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
