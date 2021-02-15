package fi.riista.config.properties;

import fi.riista.config.AwsCloudRuntimeConfig;
import fi.riista.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

@Order
public class WebAppContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOG = LoggerFactory.getLogger(WebAppContextInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        final ConfigurableEnvironment configurableEnvironment = applicationContext.getEnvironment();
        configurableEnvironment.setDefaultProfiles(Constants.STANDARD_DATABASE);

        final MutablePropertySources propertySources = configurableEnvironment.getPropertySources();

        AwsCloudRuntimeConfig.createPropertySource().ifPresent(awsPropertySource -> {
            propertySources.addLast(awsPropertySource);

            LOG.info("Using Amazon RDS profile");

            configurableEnvironment.setActiveProfiles(Constants.AMAZON_DATABASE);
        });
    }
}
