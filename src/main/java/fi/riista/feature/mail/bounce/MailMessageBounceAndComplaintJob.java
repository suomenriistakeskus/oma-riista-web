package fi.riista.feature.mail.bounce;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "MailMessageBounceAndComplaintJob",
        enabledProperty = "ses.bounce.processing.enabled",
        cronExpression = "${ses.bounce.processing.schedule}"
)
public class MailMessageBounceAndComplaintJob extends RunAsAdminJob {
    private static final Logger LOG = LoggerFactory.getLogger(MailMessageBounceAndComplaintJob.class);

    @Resource
    private MailMessageBounceAndComplaintListener mailMessageBounceAndComplaintListener;

    @Override
    public void executeAsAdmin() {
        LOG.info("Running MailMessageBounceAndComplaintJob");

        try {
            mailMessageBounceAndComplaintListener.pollForBounces();
        } catch (final Exception ex) {
            LOG.error("Mail message bounce processing has failed", ex);
        }

        try {
            mailMessageBounceAndComplaintListener.pollForComplaints();
        } catch (final Exception ex) {
            LOG.error("Mail message complaint processing has failed", ex);
        }
    }
}
