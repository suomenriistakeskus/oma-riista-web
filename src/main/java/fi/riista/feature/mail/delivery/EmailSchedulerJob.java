package fi.riista.feature.mail.delivery;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import fi.riista.feature.mail.MailService;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "MailSender",
        enabledProperty = "mail.enabled",
        fixedRate = 5_000)
public class EmailSchedulerJob extends RunAsAdminJob {
    private static final Logger LOG = LoggerFactory.getLogger(EmailSchedulerJob.class);

    @Resource
    private MailService mailService;

    @Override
    public void executeAsAdmin() {
        try {
            mailService.processOutgoingMail();
        } catch (final Exception ex) {
            LOG.error("Mail scheduler error!", ex);
        }
    }
}
