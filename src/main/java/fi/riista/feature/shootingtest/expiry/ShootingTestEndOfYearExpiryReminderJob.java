package fi.riista.feature.shootingtest.expiry;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.util.DateUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "ShootingTestEndOfYearExpiryReminderJob",
        enabledProperty = "shootingtest.endofyear.expiry.reminder.enabled",
        cronExpression = "${shootingtest.endofyear.expiry.reminder.schedule}")
public class ShootingTestEndOfYearExpiryReminderJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(ShootingTestEndOfYearExpiryReminderJob.class);

    @Resource
    private ShootingTestEndOfYearExpiryFeature expiryFeature;

    @Resource
    private MailService mailService;

    @Resource
    private ShootingTestEndOfYearExpiryEmailFactory emailFactory;

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) {
        final int expiryYear = DateUtil.currentYear() - 1;
        final Map<Long, List<ShootingTestEndOfYearExpiryDTO>> rhyIdToExpiry = expiryFeature.getOpenShootingTest(expiryYear);
        final Map<Long, Set<String>> rhyEmail = expiryFeature.resolveEmails(rhyIdToExpiry.keySet());

        LOG.info("Sending email reminders for shooting tests still open in {}...", expiryYear);

        for (final Long rhyId : rhyIdToExpiry.keySet()) {
            try {
                final List<ShootingTestEndOfYearExpiryDTO> dtos = rhyIdToExpiry.get(rhyId);
                final ShootingTestEndOfYearExpiryEmail email = emailFactory.buildEmail(dtos, rhyEmail.get(rhyId));

                mailService.send(MailMessageDTO.builder()
                        .withFrom(mailService.getDefaultFromAddress())
                        .withSubject(email.getSubject())
                        .withRecipients(email.getRecipients())
                        .appendBody("<html><head><meta charset=\"utf-8\"></head><body>")
                        .appendBody(email.getBody())
                        .appendBody("</body></html>")
                        .build());

            } catch (final Exception e) {
                LOG.error("Error sending open shooting test reminder", e);
            }
        }

        LOG.info("Done sending shooting test email reminders");
    }
}