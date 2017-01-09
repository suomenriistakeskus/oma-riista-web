package fi.riista.config;

import fi.riista.config.profile.AmazonDatabase;
import fi.riista.config.profile.EmbeddedDatabase;
import fi.riista.config.profile.StandardDatabase;
import fi.riista.config.quartz.QuartzScheduledJobRegistrar;
import fi.riista.config.quartz.QuartzSpringBeanJobFactory;

import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;

import java.io.IOException;
import java.util.Properties;

@PropertySource("classpath:configuration/scheduled.properties")
@PropertySource("classpath:configuration/quartz.properties")
@Configuration
@Conditional(value = QuartzConfig.QuartzEnabledCondition.class)
public class QuartzConfig {

    private static final String SCHEDULER_NAME = "riistakeskusScheduler";

    @Resource(name = "quartzProperties")
    private Properties quartzProperties;

    static class QuartzEnabledCondition implements ConfigurationCondition {
        @Override
        public ConfigurationPhase getConfigurationPhase() {
            return ConfigurationPhase.REGISTER_BEAN;
        }

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return context.getEnvironment().getProperty("quartz.enabled", boolean.class, false);
        }
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
            DataSource dataSource, PlatformTransactionManager transactionManager, JobFactory jobFactory) {

        final SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(quartzProperties);
        factory.setSchedulerName(SCHEDULER_NAME);
        factory.setAutoStartup(true);
        factory.setWaitForJobsToCompleteOnShutdown(true);

        // Delay added to avoid errors caused by updating registered tasks and reduce load on startup
        factory.setStartupDelay(60);

        return factory;
    }

    @Bean
    public QuartzScheduledJobRegistrar quartzScheduledJobRegistrar(Scheduler schedulerFactory) {
        final QuartzScheduledJobRegistrar registrar = new QuartzScheduledJobRegistrar();
        registrar.setScheduler(schedulerFactory);

        return registrar;
    }

    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        QuartzSpringBeanJobFactory jobFactory = new QuartzSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @AmazonDatabase
    @StandardDatabase
    @Configuration
    static class QuartzEnviroment {
        @Bean
        public Properties quartzProperties() throws IOException {
            return createQuartzProperties();
        }
    }

    @EmbeddedDatabase
    @Configuration
    static class EmbeddedDatabaseQuartzEnviroment {
        @Bean
        public Properties quartzProperties() throws IOException {
            final Properties props = createQuartzProperties();
            props.setProperty(
                    "org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
            return props;
        }
    }

    private static Properties createQuartzProperties() throws IOException {
        final PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/configuration/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

}
