package fi.riista.cli;

import fi.riista.config.BatchConfig;
import fi.riista.config.Constants;
import fi.riista.config.DataSourceConfig;
import fi.riista.config.LiquibaseConfig;
import fi.riista.config.SerializationConfig;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.integration.lupahallinta.LupahallintaImportConfig;
import fi.riista.integration.lupahallinta.club.LHHuntingClubBatchConfig;
import fi.riista.integration.lupahallinta.support.LupahallintaHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.JobOperator;
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

public class LHHuntingClubImportCli {
    private static final Logger LOG = LoggerFactory.getLogger(LHHuntingClubImportCli.class);

    @PropertySource("classpath:configuration/application.properties")
    @PropertySource("classpath:configuration/aws.properties")
    @ComponentScan(basePackageClasses = LHHuntingClubBatchConfig.class)
    @Import({
            DataSourceConfig.class, BatchConfig.class, LiquibaseConfig.class, RuntimeEnvironmentUtil.class,
            SerializationConfig.class, LupahallintaHttpClient.class, LupahallintaImportConfig.class
    })
    static class LHHuntingClubCmdContext {
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
            ctx.register(LHHuntingClubCmdContext.class);
            ctx.refresh();
            ctx.start();

            try {
                ctx.getBean(LiquibaseConfig.class).upgradeDatabase();
                ctx.getBean(JobOperator.class).startNextInstance(LHHuntingClubBatchConfig.JOB_NAME);

            } catch (Exception e) {
                LOG.error("Job execution has failed with error", e);
            }
        }
    }
}
