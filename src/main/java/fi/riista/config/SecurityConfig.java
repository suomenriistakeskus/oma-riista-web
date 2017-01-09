package fi.riista.config;

import com.google.common.base.Joiner;
import fi.riista.config.properties.SecurityConfigurationProperties;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.security.aop.CustomMethodSecurityExpressionHandler;
import fi.riista.security.authentication.CustomUserDetailsService;
import fi.riista.security.authorization.EntityPermissionEvaluator;
import fi.riista.security.jwt.JwtAuthenticationProvider;
import fi.riista.security.otp.OneTimePasswordAuthenticationProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Configuration
@PropertySource("classpath:configuration/security.properties")
@ComponentScan(basePackages = Constants.SECURITY_BASE_PACKAGE)
public class SecurityConfig {
    @Bean
    public SecureRandom globalSecureRandom() throws NoSuchAlgorithmException {
        return SecureRandom.getInstance("SHA1PRNG");
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        final RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(Joiner.on('\n').join(new String[]{
                SystemUser.Role.ROLE_ADMIN.includes(SystemUser.Role.ROLE_USER),
                SystemUser.Role.ROLE_MODERATOR.includes(SystemUser.Role.ROLE_USER)
        }));
        return hierarchy;
    }

    @EnableGlobalMethodSecurity(prePostEnabled = true, order = AopConfig.ORDER_METHOD_SECURITY)
    public static class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {
        @Resource
        private RoleHierarchy roleHierarchy;

        @Override
        protected MethodSecurityExpressionHandler createExpressionHandler() {
            DefaultMethodSecurityExpressionHandler expressionHandler = new CustomMethodSecurityExpressionHandler();
            expressionHandler.setRoleHierarchy(roleHierarchy);
            expressionHandler.setPermissionEvaluator(permissionEvaluator());
            return expressionHandler;
        }

        @Bean
        public PermissionEvaluator permissionEvaluator() {
            return new EntityPermissionEvaluator();
        }

        @Bean
        @Override
        public AuthenticationManager authenticationManager() throws Exception {
            return super.authenticationManager();
        }
    }

    @Configuration
    public static class AuthenticationManagerConfiguration extends GlobalAuthenticationConfigurerAdapter {
        @Resource
        private ApplicationEventPublisher applicationEventPublisher;

        @Override
        public void init(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(daoAuthenticationProvider())
                    .authenticationProvider(jwtAuthenticationProvider())
                    .authenticationProvider(preAuthenticatedAuthenticationProvider())
                    .authenticationEventPublisher(new DefaultAuthenticationEventPublisher(applicationEventPublisher))
                    .eraseCredentials(true);
        }

        @Bean
        public DaoAuthenticationProvider daoAuthenticationProvider() {
            final OneTimePasswordAuthenticationProvider provider = new OneTimePasswordAuthenticationProvider();
            provider.setUserDetailsService(userDetailsService());
            provider.setPasswordEncoder(passwordEncoder());
            return provider;
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return new CustomUserDetailsService();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder(10);
        }

        @Bean
        public JwtAuthenticationProvider jwtAuthenticationProvider() {
            return new JwtAuthenticationProvider(securityConfigurationProperties().getJwtSecret(), userDetailsService());
        }

        @Bean
        public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider() {
            final PreAuthenticatedAuthenticationProvider provider =
                    new PreAuthenticatedAuthenticationProvider();
            provider.setPreAuthenticatedUserDetailsService(
                    new UserDetailsByNameServiceWrapper<>(userDetailsService()));
            return provider;
        }

        @Bean
        public SecurityConfigurationProperties securityConfigurationProperties() {
            return new SecurityConfigurationProperties();
        }
    }
}
