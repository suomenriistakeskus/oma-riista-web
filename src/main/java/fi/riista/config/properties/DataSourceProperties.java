package fi.riista.config.properties;

import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DBInstanceNotFoundException;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.zaxxer.hikari.HikariConfig;
import fi.riista.feature.RuntimeEnvironmentUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Configuration
@PropertySource({"classpath:configuration/db.properties", "classpath:configuration/aws.properties"})
public class DataSourceProperties {

    @Value("${db.driver}")
    private String driverClassName;

    @Value("${db.url}")
    private String jdbcUrl;

    @Value("${db.max.connections}")
    private int maximumPoolSize;

    @Value("${db.username}")
    private String username;

    @Value("${db.password}")
    private String password;

    @Value("${aws.rds.instanceId}")
    private String rdsInstanceId;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    public boolean isGisQuerySupported() {
        return driverClassName != null && driverClassName.contains("postgres");
    }

    private HikariConfig getCommonConfig() {
        final HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setPoolName("defaultDatabase");
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setMaximumPoolSize(maximumPoolSize);
        hikariConfig.setConnectionTimeout(TimeUnit.SECONDS.toMillis(30));
        hikariConfig.setIdleTimeout(TimeUnit.MINUTES.toMillis(1));
        hikariConfig.setLeakDetectionThreshold(TimeUnit.MINUTES.toMillis(10));
        hikariConfig.setMaxLifetime(TimeUnit.MINUTES.toMillis(15));

        if (runtimeEnvironmentUtil.isProductionEnvironment()) {
            hikariConfig.setInitializationFailTimeout(-1);
        }

        return hikariConfig;
    }

    public HikariConfig buildStandardPoolConfig() {
        final HikariConfig hikariConfig = getCommonConfig();

        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        return hikariConfig;
    }

    public HikariConfig buildAmazonPoolConfig(final AmazonRDSClient amazonRds) {
        final DBInstance dbInstance = getDbInstance(amazonRds, rdsInstanceId);
        final HikariConfig hikariConfig = getCommonConfig();

        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setJdbcUrl("jdbc:postgresql://" +
                dbInstance.getEndpoint().getAddress() + ":" +
                dbInstance.getEndpoint().getPort() + "/" +
                "riistakeskus");

        return hikariConfig;
    }

    private static DBInstance getDbInstance(final AmazonRDSClient amazonRds, final String rdsInstanceId) {
        try {
            final DescribeDBInstancesRequest request = new DescribeDBInstancesRequest()
                    .withDBInstanceIdentifier(rdsInstanceId);
            final DescribeDBInstancesResult response = amazonRds.describeDBInstances(request);

            return response.getDBInstances().get(0);

        } catch (final DBInstanceNotFoundException e) {
            throw new IllegalStateException("Could not find DB instance!");
        }
    }
}
