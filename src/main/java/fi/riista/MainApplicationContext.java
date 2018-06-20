package fi.riista;

import fi.riista.config.AopConfig;
import fi.riista.config.AsyncConfig;
import fi.riista.config.AwsCloudConfig;
import fi.riista.config.BatchConfig;
import fi.riista.config.CacheConfig;
import fi.riista.config.Constants;
import fi.riista.config.DataSourceConfig;
import fi.riista.config.ExecutorConfig;
import fi.riista.config.HandlebarsConfig;
import fi.riista.config.HttpClientConfig;
import fi.riista.config.JPAConfig;
import fi.riista.config.JaxbConfig;
import fi.riista.config.LiquibaseConfig;
import fi.riista.config.LocalizationConfig;
import fi.riista.config.MailConfig;
import fi.riista.config.PapertrailConfig;
import fi.riista.config.QuartzConfig;
import fi.riista.config.SchedulingConfig;
import fi.riista.config.SecurityConfig;
import fi.riista.config.SentryConfig;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.config.SerializationConfig;
import fi.riista.config.properties.EncryptedProperties;
import fi.riista.feature.account.certificate.HuntingCardQRCodeKeyHolder;
import fi.riista.util.JCEUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.security.Security;

@Configuration
@Import({
        DataSourceConfig.class,
        JPAConfig.class,
        LiquibaseConfig.class,
        CacheConfig.class,
        LocalizationConfig.class,
        HttpClientConfig.class,
        ExecutorConfig.class,
        AsyncConfig.class,
        SchedulingConfig.class,
        HandlebarsConfig.class,
        MailConfig.class,
        SecurityConfig.class,
        AopConfig.class,
        SerializationConfig.class,
        JaxbConfig.class,
        BatchConfig.class,
        QuartzConfig.class,
        AwsCloudConfig.class,
        SentryConfig.class,
        PapertrailConfig.class
})
@PropertySource("classpath:git.properties")
@PropertySource("classpath:configuration/application.properties")
@ComponentScan({Constants.FEATURE_BASE_PACKAGE, Constants.INTEGRATION_BASE_PACKAGE})
public class MainApplicationContext {
    static {
        JCEUtil.removeJavaCryptographyAPIRestrictions();
        Security.addProvider(new BouncyCastleProvider());
    }

    @Resource
    private HuntingCardQRCodeKeyHolder qrCodeKeyHolder;

    @PostConstruct
    public void afterStartup() {
        qrCodeKeyHolder.decodePrivateKey();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        final String password = System.getProperty("JASYPT_PASSWORD");

        if (StringUtils.hasText(password)) {
            final StandardPBEStringEncryptor se = new StandardPBEStringEncryptor();
            se.setConfig(EncryptedProperties.createPBEConfig(password));
            return new EncryptedProperties.PlaceholderConfigurer(se);
        }
        return new PropertySourcesPlaceholderConfigurer();
    }
}
