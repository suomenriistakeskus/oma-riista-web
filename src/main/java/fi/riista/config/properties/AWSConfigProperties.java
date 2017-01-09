package fi.riista.config.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import static org.springframework.util.StringUtils.hasText;

// Property names are based on default Amazon Beanstalk environment variable names
@Configuration
@PropertySource("classpath:configuration/aws.properties")
public class AWSConfigProperties {
    @Value("${file.storage.s3.bucket.default}")
    private String bucketDefault;

    @Value("${file.storage.s3.bucket.diary_image}")
    private String bucketDiaryImages;

    public boolean isConfigured() {
        return hasText(bucketDefault) && hasText(bucketDiaryImages);
    }

    public String getDefaultBucket() {
        return bucketDefault;
    }

    public String getBucketDiaryImages() {
        return bucketDiaryImages;
    }
}
