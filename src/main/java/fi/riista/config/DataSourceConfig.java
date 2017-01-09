package fi.riista.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.spatial.GeoDBTemplates;
import com.querydsl.sql.spatial.PostGISTemplates;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import com.zaxxer.hikari.HikariDataSource;
import fi.riista.config.profile.AmazonDatabase;
import fi.riista.config.profile.EmbeddedDatabase;
import fi.riista.config.profile.StandardDatabase;
import fi.riista.config.properties.DataSourceProperties;
import org.springframework.cloud.aws.core.region.RegionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
@Import(DataSourceProperties.class)
public class DataSourceConfig {

    @Bean
    public SQLQueryFactory sqlQueryFactory(DataSource dataSource, SQLTemplates templates) {
        final com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(templates);
        configuration.setExceptionTranslator(new SpringExceptionTranslator());
        return new SQLQueryFactory(configuration, dataSource);
    }

    @Configuration
    @StandardDatabase
    static class StandardDatabaseConfiguration {
        @Bean(destroyMethod = "close")
        public DataSource dataSource(DataSourceProperties dataSourceProperties) {
            return new HikariDataSource(dataSourceProperties.buildStandardPoolConfig());
        }

        @Bean
        public SQLTemplates queryDslSqlTemplates() {
            return PostGISTemplates.DEFAULT;
        }
    }

    @Configuration
    @AmazonDatabase
    static class AmazonDatabaseConfiguration {
        @Bean(destroyMethod = "close")
        public DataSource dataSource(
                final DataSourceProperties dataSourceProperties,
                final AWSCredentialsProvider credentialsProvider,
                final RegionProvider regionProvider) {
            final AmazonRDSClient amazonRds = new AmazonRDSClient(credentialsProvider);
            amazonRds.setRegion(regionProvider.getRegion());

            return new HikariDataSource(dataSourceProperties.buildAmazonPoolConfig(amazonRds));
        }

        @Bean
        public SQLTemplates queryDslSqlTemplates() {
            return PostGISTemplates.DEFAULT;
        }
    }

    @Configuration
    @EmbeddedDatabase
    static class TestDatabaseConfiguration {
        @Bean(destroyMethod = "shutdown")
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
        }

        @Bean
        public SQLTemplates queryDslSqlTemplates() {
            return GeoDBTemplates.DEFAULT;
        }
    }
}
