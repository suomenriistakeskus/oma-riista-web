package fi.riista.config;

import com.querydsl.jpa.HQLTemplates;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.config.jpa.CustomH2Dialect;
import fi.riista.config.jpa.CustomPostgisDialect;
import fi.riista.config.jpa.ImprovedPhysicalNamingStrategyForTestSetup;
import fi.riista.config.profile.AmazonDatabase;
import fi.riista.config.profile.EmbeddedDatabase;
import fi.riista.config.profile.StandardDatabase;
import fi.riista.config.properties.JPAProperties;
import fi.riista.feature.common.repository.BaseRepositoryImpl;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.Map;

@Configuration
@Import(JPAProperties.class)
@EnableTransactionManagement(mode = AdviceMode.PROXY, order = AopConfig.ORDER_TRANSACTION)
@EnableJpaRepositories(basePackages = Constants.APPLICATION_ROOT_PACKAGE, repositoryBaseClass = BaseRepositoryImpl.class)
public class JPAConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPQLQueryFactory jpqlQueryFactory() {
        return new JPAQueryFactory(new HQLTemplates(), entityManager);
    }

    @Configuration
    @AmazonDatabase
    @StandardDatabase
    static class StandardJpaConfiguration {
        @Resource
        protected DataSource dataSource;

        @Resource
        protected JPAProperties jpaPropertiesBuilder;

        @Resource
        protected LiquibaseConfig liquibaseConfig;

        @Resource
        private DataSourceConfig.DataSourceContext dataSourceWrapper;

        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
            final LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();

            emf.setJpaDialect(new HibernateJpaDialect());
            emf.setJpaVendorAdapter(jpaVendorAdapter());
            emf.setJpaPropertyMap(jpaProperties());
            emf.setPackagesToScan(Constants.APPLICATION_ROOT_PACKAGE);
            emf.setDataSource(dataSource);
            emf.setMappingResources(dataSourceWrapper.getMappingResources().orElse(null));

            // Run Liquibase migrations before ORM setup
            liquibaseConfig.upgradeDatabase();

            return emf;
        }

        @Bean
        public HibernateJpaVendorAdapter jpaVendorAdapter() {
            final HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
            jpaVendorAdapter.setDatabasePlatform(getDatabasePlatform());
            return jpaVendorAdapter;
        }

        @Bean
        public JpaTransactionManager transactionManager() {
            return new JpaTransactionManager(entityManagerFactory().getObject());
        }

        @Bean
        public PersistenceExceptionTranslationPostProcessor exceptionTranslationPostProcessor() {
            return new PersistenceExceptionTranslationPostProcessor();
        }

        public String getDatabasePlatform() {
            return CustomPostgisDialect.class.getCanonicalName();
        }

        protected Map<String, Object> jpaProperties() {
            return jpaPropertiesBuilder.build();
        }
    }

    @Configuration
    @EmbeddedDatabase
    static class EmbeddedJpaConfiguration extends StandardJpaConfiguration {
        @Override
        public String getDatabasePlatform() {
            return CustomH2Dialect.class.getCanonicalName();
        }

        @Override
        protected Map<String, Object> jpaProperties() {
            final Map<String, Object> jpaProperties = this.jpaPropertiesBuilder.build();
            jpaProperties.put(AvailableSettings.GENERATE_STATISTICS, true);
            jpaProperties.put(
                    AvailableSettings.PHYSICAL_NAMING_STRATEGY, ImprovedPhysicalNamingStrategyForTestSetup.class.getName());
            return jpaProperties;
        }
    }
}
