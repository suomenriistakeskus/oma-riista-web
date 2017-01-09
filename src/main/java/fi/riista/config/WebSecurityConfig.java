package fi.riista.config;

import fi.riista.api.pub.AccountRegistrationApiResource;
import fi.riista.api.pub.HealthCheckController;
import fi.riista.config.properties.SecurityConfigurationProperties;
import fi.riista.security.aop.CustomWebSecurityExpressionHandler;
import fi.riista.security.audit.LogoutAuditEventListener;
import fi.riista.security.authentication.CustomAuthenticationFailureHandler;
import fi.riista.security.authentication.CustomAuthenticationSuccessHandler;
import fi.riista.security.authentication.CustomSpringSessionRememberMeServices;
import fi.riista.security.authorization.CustomAccessDeniedHandler;
import fi.riista.security.csrf.CsrfCookieGeneratorFilter;
import fi.riista.security.jwt.JwtAuthenticationFilter;
import fi.riista.security.otp.OneTimePasswordFilterConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.csrf.LazyCsrfTokenRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.annotation.Resource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String PATTERN_API = "/api/**";

    // Public JSON(P) API
    private static final String PATTERN_ANONYMOUS_API = "/api/v1/anon/**";

    // Mobile API
    private static final String PATTERN_MOBILE_API = "/api/mobile/**";

    // Import and export API restricted using IP whitelist
    private static final String PATTERN_EXPORT_API = "/api/v1/export/**";
    private static final String PATTERN_IMPORT_API = "/api/v1/import/**";
    private static final String PATTERN_ADMIN_API = "/api/v1/admin/**";

    // IP address restricted API for uploading moose data cards
    private static final String PATTERN_MOOSE_DATA_CARD_UPLOAD = "/api/v1/anon/moosedatacard/upload";

    // Vetuma registration return callback
    private static final String PATTERN_VETUMA = "/vetuma/**";

    // Login and logout
    private static final String URI_LOGIN = "/login";
    private static final String URI_LOGOUT = "/logout";

    private static final String[] IGNORE_CSRF_PATTERN = {
            PATTERN_EXPORT_API,
            PATTERN_IMPORT_API,
            PATTERN_ANONYMOUS_API,
            PATTERN_MOBILE_API,

            AccountRegistrationApiResource.URI_SEND_EMAIL,
            AccountRegistrationApiResource.URI_FROM_EMAIL,

            // Vetuma does not know how to include CSRF header, but is protected using TRID parameter
            PATTERN_VETUMA,

            // Login cannot be protected, because mobile client does not support CSRF currently
            URI_LOGIN,

            // Ignore logout because session could be expired and CSRF cookie missing
            URI_LOGOUT
    };

    private static final String[] SKIP_CSRF_COOKIE_GENERATION = {
            HealthCheckController.URI_HEALTH_CHECK,
            PATTERN_EXPORT_API,
            PATTERN_IMPORT_API,
            PATTERN_ANONYMOUS_API,
            PATTERN_MOBILE_API,
    };

    @Resource
    private SecurityConfigurationProperties securityConfigurationProperties;

    @Resource
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Resource
    private CustomAuthenticationFailureHandler authenticationFailureHandler;

    @Resource
    private LogoutAuditEventListener logoutAuditEventListener;

    @Resource
    private CustomSpringSessionRememberMeServices rememberMeServices;

    @Resource
    private SecurityExpressionHandler<FilterInvocation> webSecurityExpressionHandler;

    @Bean
    public HttpStatusEntryPoint authenticationEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        final HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName(CsrfCookieGeneratorFilter.ANGULAR_CSRF_DEFAULT_HEADER_NAME);
        return repository;
    }

    @Bean
    public SecurityExpressionHandler<FilterInvocation> webSecurityExpressionHandler(
            PermissionEvaluator permissionEvaluator, RoleHierarchy roleHierarchy) {
        final DefaultWebSecurityExpressionHandler handler = new CustomWebSecurityExpressionHandler();

        handler.setRoleHierarchy(roleHierarchy);
        handler.setPermissionEvaluator(permissionEvaluator);

        return handler;
    }

    @Override
    public void configure(WebSecurity web) {
        web.expressionHandler(webSecurityExpressionHandler)
                .ignoring().antMatchers(
                "/static/**",
                "/frontend/**",
                "/favicon.ico"
        );
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // Replace normal form-login with one-time-token filter
        httpSecurity
                .formLogin().disable()
                .apply(new OneTimePasswordFilterConfigurer(
                        URI_LOGIN,
                        authenticationSuccessHandler,
                        authenticationFailureHandler,
                        authenticationEntryPoint()));

        final JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(
                authenticationManager(), authenticationEntryPoint());
        httpSecurity.addFilterBefore(jwtFilter, BasicAuthenticationFilter.class);

        final CsrfCookieGeneratorFilter csrfFilter = new CsrfCookieGeneratorFilter(SKIP_CSRF_COOKIE_GENERATION);
        httpSecurity.addFilterAfter(csrfFilter, SessionManagementFilter.class);

        httpSecurity
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                .and()

                .httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint())
                .and()

                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher(URI_LOGOUT, "POST"))
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
                .addLogoutHandler(logoutAuditEventListener)
                .and()

                .rememberMe()
                .rememberMeServices(rememberMeServices)
                .and()

                .requestCache()
                .requestCache(new NullRequestCache())
                .and()

                .headers()
                .cacheControl().disable()
                .and()

                .csrf()
                .csrfTokenRepository(new LazyCsrfTokenRepository(csrfTokenRepository()))
                .ignoringAntMatchers(IGNORE_CSRF_PATTERN)
                .and()

                .exceptionHandling()
                .accessDeniedHandler(new CustomAccessDeniedHandler())
                .authenticationEntryPoint(authenticationEntryPoint());

        httpSecurity
                .authorizeRequests()
                .expressionHandler(webSecurityExpressionHandler)

                .antMatchers(
                        "/",
                        "/api/v1/language",
                        "/api/v1/validation/phonenumber",
                        "/api/v1/validation/email",
                        "/api/v1/password/forgot",
                        "/api/v1/password/reset",
                        "/api/v1/password/verifytoken",
                        "/api/v1/register/**",

                        HealthCheckController.URI_HEALTH_CHECK,
                        PATTERN_ANONYMOUS_API,
                        PATTERN_VETUMA
                ).permitAll()

                .antMatchers(PATTERN_MOOSE_DATA_CARD_UPLOAD)
                .hasIpAddress(securityConfigurationProperties.getIpWhitelistForMooseDataCardUpload())

                .antMatchers(PATTERN_IMPORT_API).access("matchesWhiteList() and hasRole('ROLE_REST')")
                .antMatchers(PATTERN_EXPORT_API).access("matchesWhiteList() and hasRole('ROLE_REST')")

                // Restrict admin API by IP whiteList
                .antMatchers(PATTERN_ADMIN_API).hasRole("ADMIN")
                .antMatchers(PATTERN_API).hasRole("USER")

                .anyRequest().denyAll();
    }
}
