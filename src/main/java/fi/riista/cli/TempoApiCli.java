package fi.riista.cli;

import fi.riista.config.Constants;
import fi.riista.config.DataSourceConfig;
import fi.riista.config.HttpClientConfig;
import fi.riista.config.JPAConfig;
import fi.riista.config.LiquibaseConfig;
import fi.riista.config.PapertrailConfig;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
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
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

public class TempoApiCli {
    private static final Logger LOG = LoggerFactory.getLogger(TempoApiCli.class);

    @PropertySource("configuration/application.properties")
    @ComponentScan(basePackageClasses = TempoApiConfiguration.class)
    @Import({
            HttpClientConfig.class,
            DataSourceConfig.class,
            JPAConfig.class,
            LiquibaseConfig.class,
            PapertrailConfig.class,
            CustomJacksonObjectMapper.class,
            RuntimeEnvironmentUtil.class
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

        @Bean
        public ClientHttpRequestFactory clientHttpRequestFactory() {
            return new SimpleClientHttpRequestFactory();
        }
    }

    public static void main(final String[] cmdArgs) {
        try (final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().addActiveProfile(Constants.STANDARD_DATABASE);
            ctx.register(Context.class);
            ctx.refresh();
            ctx.start();

            try {
                final SrvaUpdateCallRingFeature shortnumbersFeature = ctx.getBean(SrvaUpdateCallRingFeature.class);
                shortnumbersFeature.configureAll();

            } catch (Exception e) {
                LOG.error("Job execution has failed with error", e);
            }
        }
    }
}
