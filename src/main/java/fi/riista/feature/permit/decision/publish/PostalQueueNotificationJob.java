package fi.riista.feature.permit.decision.publish;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "PostalQueueNotificationJob",
        enabledProperty = "permit.decision.postal.queue.notification.enabled",
        cronExpression = "${permit.decision.postal.queue.notification.schedule}"
)
public class PostalQueueNotificationJob extends RunAsAdminJob {
    private static final Logger LOG = LoggerFactory.getLogger(PostalQueueNotificationJob.class);

    @Resource
    private PostalQueueNotificationFeature postalQueueNotificationFeature;

    @Override
    public void executeAsAdmin() {
        try {
            LOG.info("Starting ...");
            postalQueueNotificationFeature.sendPostalQueueNotification();
            LOG.info("Done.");
        } catch (Exception e) {
            LOG.error("Processing threw exception", e);
        }
    }

}
