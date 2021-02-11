package fi.riista.config;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Configuration
public class LiquibaseConfig {
    private static final String LIQUIBASE_CHANGELOG_LOCATION = "classpath:migrations/db.changelog.xml";

    @Resource
    private DataSource dataSource;

    @Resource
    private ResourceLoader resourceLoader;

    public void upgradeDatabase() {
        try {
            getSpringLiquibase().afterPropertiesSet();
        } catch (LiquibaseException e) {
            throw new RuntimeException(e);
        }
    }

    private SpringLiquibase getSpringLiquibase() {
        final SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog(LIQUIBASE_CHANGELOG_LOCATION);
        liquibase.setResourceLoader(resourceLoader);
        liquibase.setDataSource(dataSource);
        return liquibase;
    }
}
