package fi.riista.cli;

import fi.riista.config.Constants;
import fi.riista.config.DataSourceConfig;
import fi.riista.config.HttpClientConfig;
import fi.riista.config.JPAConfig;
import fi.riista.config.LiquibaseConfig;
import fi.riista.config.PapertrailConfig;
import fi.riista.config.SerializationConfig;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.integration.srva.callring.SrvaUpdateCallRingFeature;
import fi.riista.integration.srva.callring.TempoApiConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.UUID;

public class TempoApiCli {
    private static final Logger LOG = LoggerFactory.getLogger(TempoApiCli.class);

    @PropertySource({"configuration/application.properties", "configuration/aws.properties"})
    @ComponentScan(basePackageClasses = TempoApiConfiguration.class)
    @Import({
            HttpClientConfig.class,
            DataSourceConfig.class,
            JPAConfig.class,
            LiquibaseConfig.class,
            PapertrailConfig.class,
            RuntimeEnvironmentUtil.class,
            SerializationConfig.class
    })
    static class Context {
        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Resource
    private static ActiveUserService activeUserService;

    public static void main(final String[] cmdArgs) {
        try (final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().addActiveProfile(Constants.STANDARD_DATABASE);
            ctx.register(Context.class);
            ctx.refresh();
            ctx.start();

            try {
                activeUserService.loginWithoutCheck(createUser());

                final SrvaUpdateCallRingFeature shortnumbersFeature = ctx.getBean(SrvaUpdateCallRingFeature.class);
                shortnumbersFeature.configureAll();

            } catch (Exception e) {
                LOG.error("Job execution has failed with error", e);
            }
        }
    }

    private static SystemUser createUser() {
        final SystemUser u = new SystemUser() {
            @Override
            public String getHashedPassword() {
                return UUID.randomUUID().toString();
            }
        };
        u.setId(ActiveUserService.SCHEDULED_TASK_USER_ID);
        u.setUsername(TempoApiCli.class.getSimpleName());
        u.setRole(SystemUser.Role.ROLE_ADMIN);
        return u;
    }
}
