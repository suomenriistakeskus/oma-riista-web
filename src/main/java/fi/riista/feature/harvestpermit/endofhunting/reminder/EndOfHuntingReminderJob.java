package fi.riista.feature.harvestpermit.endofhunting.reminder;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import fi.riista.feature.harvestpermit.endofhunting.reminder.EndOfHuntingReminderEmailFactory.EndOfHuntingReminderEmailType;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.harvestpermit.endofhunting.reminder.EndOfHuntingReminderFeature.EndOfHuntingReminderType.ALL;
import static fi.riista.feature.harvestpermit.endofhunting.reminder.EndOfHuntingReminderFeature.EndOfHuntingReminderType.MULTI_YEAR;
import static fi.riista.feature.harvestpermit.endofhunting.reminder.EndOfHuntingReminderFeature.EndOfHuntingReminderType.ONE_YEAR;
import static fi.riista.feature.harvestpermit.endofhunting.reminder.EndOfHuntingReminderEmailFactory.EndOfHuntingReminderEmailType.FIRST_REMINDER;
import static fi.riista.feature.harvestpermit.endofhunting.reminder.EndOfHuntingReminderEmailFactory.EndOfHuntingReminderEmailType.SECOND_REMINDER;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "EndOfHuntingReminderJob",
        enabledProperty = "harvestpermit.endofhunting.reminder.enabled",
        cronExpression = "${harvestpermit.endofhunting.reminder.schedule}")
public class EndOfHuntingReminderJob extends RunAsAdminJob {

    private static final Logger LOG = LoggerFactory.getLogger(EndOfHuntingReminderJob.class);

    @Resource
    private EndOfHuntingReminderFeature endOfHuntingReminderFeature;

    @Resource
    private EndOfHuntingReminderEmailFactory emailFactory;

    @Resource
    private MailService mailService;

    @Override
    public void executeAsAdmin() {
        LOG.info("Checking missing end of hunting reports");

        final LocalDate today = DateUtil.today();

        // The first reminder when permit end date yesterday
        final List<EndOfHuntingReminderDTO> missingReportsFirstReminder =
                endOfHuntingReminderFeature.getMissingEndOfHuntingReports(today.minusDays(1), ALL);
        if (!missingReportsFirstReminder.isEmpty()) {
            sendMails(FIRST_REMINDER, missingReportsFirstReminder);
        }

        // For one year permits the second reminder week after the first reminder
        final List<EndOfHuntingReminderDTO> missingReportsSecondReminder =
                endOfHuntingReminderFeature.getMissingEndOfHuntingReports(today.minusDays(8), ONE_YEAR);
        if (!missingReportsSecondReminder.isEmpty()) {
            sendMails(SECOND_REMINDER, missingReportsSecondReminder);
        }

        // For multi year permits the second reminder on the 8th of January the next year
        if (today.getDayOfMonth() == 8 && today.getMonthOfYear() == 1) {
            final List<EndOfHuntingReminderDTO> missingReportsMultiYearPermits =
                    endOfHuntingReminderFeature.getMissingEndOfHuntingReports(today.getYear() - 1, MULTI_YEAR);
            if (!missingReportsMultiYearPermits.isEmpty()) {
                sendMails(SECOND_REMINDER, missingReportsMultiYearPermits);
            }

            LOG.info("Sent {} second reminders of missing end of hunting report for multiyear permits", missingReportsMultiYearPermits.size());
        }
    }

    private void sendMails(final EndOfHuntingReminderEmailType emailType,
                           final List<EndOfHuntingReminderDTO> missingReports) {
        for (final EndOfHuntingReminderDTO missingReport : missingReports) {
            try {
                final EndOfHuntingReminderEmail email = emailFactory.build(emailType, missingReport);

                mailService.send(MailMessageDTO.builder()
                        .withFrom(mailService.getDefaultFromAddress())
                        .withSubject(email.getSubject())
                        .withRecipients(email.getRecipients())
                        .appendBody("<html><head><meta charset=\"utf-8\"></head><body>")
                        .appendBody(email.getBody())
                        .appendBody("</body></html>")
                        .build());

                String reminderType = emailType == FIRST_REMINDER ? "first reminder" : "second reminder";
                LOG.info("Sent " + reminderType + " of missing end of hunting permit: {}", email.getRecipients());

            } catch (final Exception e) {
                LOG.error("Error sending missing end of hunting report reminder", e);
            }
        }
    }
}
