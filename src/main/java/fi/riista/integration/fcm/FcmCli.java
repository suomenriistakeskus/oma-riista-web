package fi.riista.integration.fcm;

import de.bytefish.fcmjava.client.FcmClient;
import de.bytefish.fcmjava.model.builders.FcmMessageOptionsBuilder;
import de.bytefish.fcmjava.model.options.FcmMessageOptions;
import de.bytefish.fcmjava.requests.builders.NotificationPayloadBuilder;
import de.bytefish.fcmjava.requests.notification.NotificationPayload;
import de.bytefish.fcmjava.requests.notification.NotificationUnicastMessage;
import de.bytefish.fcmjava.responses.FcmMessageResponse;
import de.bytefish.fcmjava.responses.FcmMessageResultItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import java.time.Duration;

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
                final FcmClient fcmClient = ctx.getBean(FcmClient.class);
                final FcmMessageOptions messageOptions = new FcmMessageOptionsBuilder()
                        .setTimeToLive(Duration.ofDays(7))
                        .build();

                final NotificationPayload notificationPayload = new NotificationPayloadBuilder()
                        .setSound("default")
                        .setColor("#33aa99")
                        .setBody("Hello World")
                        .build();

                final NotificationUnicastMessage notification = new NotificationUnicastMessage(
                        messageOptions, to, notificationPayload);
                final FcmMessageResponse response = fcmClient.send(notification);

                LOG.info("{} errors {} success", response.getNumberOfFailure(), response.getNumberOfSuccess());

                for (FcmMessageResultItem resultItem : response.getResults()) {
                    LOG.info("resultItem: {}", resultItem);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
