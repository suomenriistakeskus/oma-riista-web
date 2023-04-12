package fi.riista.config;

import com.amazonaws.services.s3.AmazonS3;
import fi.riista.feature.RuntimeEnvironmentUtil;
import org.springframework.cloud.aws.core.io.s3.SimpleStorageResourceLoader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.Optional;

@Configuration
@ImportResource("classpath:/aws.xml")
@PropertySource("classpath:configuration/aws.properties")
@PropertySource("classpath:configuration/application.properties")
public class AwsCloudRuntimeConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public SimpleStorageResourceLoader simpleStorageResourceLoader(AmazonS3 amazonS3) {
        return new SimpleStorageResourceLoader(amazonS3);
    }

    public static Optional<ResourcePropertySource> createPropertySource() {
        try (final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(AwsCloudRuntimeConfig.class);
            ctx.register(RuntimeEnvironmentUtil.class);
            ctx.refresh();
            ctx.start();

            if (ctx.getBean(RuntimeEnvironmentUtil.class).isAwsEnvironment()) {
                final String s3config = ctx.getEnvironment().getProperty("aws.config.s3location");
                final Resource resource = ctx.getBean(SimpleStorageResourceLoader.class).getResource(s3config);
                return Optional.of(new ResourcePropertySource("aws", resource));
            }

            return Optional.empty();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
