package fi.riista.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.TransferManager;
import fi.riista.config.properties.AWSConfigProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.aws.core.task.ShutdownSuppressingExecutorServiceAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.task.TaskExecutor;

@Configuration
@Import(AWSConfigProperties.class)
@ImportResource("classpath:/aws.xml")
public class AwsCloudConfig {
    @Bean
    public TransferManager transferManager(AmazonS3 amazonS3, @Qualifier("awsExecutor") TaskExecutor awsExecutor) {
        return new TransferManager(amazonS3, new ShutdownSuppressingExecutorServiceAdapter(awsExecutor));
    }
}
