package fi.riista.integration.fcm;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.singletonMap;

public class FcmCli {
    private static final Logger LOG = LoggerFactory.getLogger(FcmCli.class);

    @Configuration
    @PropertySource("classpath:configuration/application.properties")
    @Import(FcmConfig.class)
    public static class Context {
    }

    public static void main(String[] args) {
        final String to = "";

        try (final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(Context.class);
            ctx.refresh();
            ctx.start();

            try {
                final FcmMulticastSender sender = ctx.getBean(FcmMulticastSender.class);

                final AndroidConfig androidConfig = AndroidConfig.builder()
                        .setTtl(Duration.ofDays(7).getSeconds())
                        .build();

                final Notification notification = Notification.builder()
                        .setTitle("Message")
                        .setBody("Hello World")
                        .build();

                final Map<String, String> data = singletonMap("announcement", "Test message");
                final MulticastMessage msg = MulticastMessage.builder()
                        .setAndroidConfig(androidConfig)
                        .putAllData(data)
                        .addAllTokens(Collections.singletonList(to))
                        .setNotification(notification)
                        .build();

                final BatchResponse response = sender.send(msg).get();

                LOG.info("{} errors {} success", response.getFailureCount(), response.getSuccessCount());

                for (final SendResponse resultItem : response.getResponses()) {
                    LOG.info("resultItem: {}", resultItem);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
