package fi.riista.feature.organization.jht.expiry;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "JHTOccupationExpiryJob",
        enabledProperty = "jht.expiry.reminder.enabled",
        cronExpression = "${jht.expiry.reminder.schedule}"
)
public class JHTOccupationExpiryJob extends RunAsAdminJob {
    private static final Logger LOG = LoggerFactory.getLogger(JHTOccupationExpiryJob.class);

    @Resource
    private JHTOccupationExpiryResolver jhtOccupationExpiryResolver;

    @Resource
    private JHTOccupationExpiryEmailFactory jhtOccupationExpiryEmailFactory;

    @Resource
    private MailService mailService;

    @Override
    public void executeAsAdmin() {
        // Training needs to be refreshed when there is less than 6 months left of the nomination.
        // Subtract two days to avoid sending email when training is not available yet.
        final LocalDate expiryDate = DateUtil.today().plusMonths(6).minusDays(2);

        final List<JHTOccupationExpiryDTO> reminderList = jhtOccupationExpiryResolver.resolve(expiryDate);
        final Map<Long, Set<String>> rhyEmailMapping = jhtOccupationExpiryResolver.resolveRhyEmails(reminderList);

        LOG.info("Sending email reminders for JHT occupations expiring on {}...", expiryDate);

        for (final JHTOccupationExpiryDTO dto : reminderList) {
            try {
                final JHTOccupationExpiryEmail email = jhtOccupationExpiryEmailFactory.buildEmail(dto, rhyEmailMapping);

                mailService.send(MailMessageDTO.builder()
                        .withFrom(mailService.getDefaultFromAddress())
                        .withRecipients(email.getRecipients())
                        .withSubject(email.getSubject())
                        .appendBody("<html><head><meta charset=\"utf-8\"></head><body>")
                        .appendBody(email.getBody())
                        .appendBody("</body></html>")
                        .build());

            } catch (final Exception e) {
                LOG.error("Error sending JHT occupation expiry reminder", e);
            }
        }

        LOG.info("Done sending email reminders");
    }
}
