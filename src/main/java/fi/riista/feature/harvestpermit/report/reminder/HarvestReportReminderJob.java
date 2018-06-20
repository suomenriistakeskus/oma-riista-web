package fi.riista.feature.harvestpermit.report.reminder;

import fi.riista.config.quartz.QuartzScheduledJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "HarvestReportReminder",
        enabledProperty = "harvest.reminder.enabled",
        cronExpression = "${harvest.reminder.schedule}"
)
public class HarvestReportReminderJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestReportReminderJob.class);

    @Resource
    private HarvestReportReminderFeature harvestReportReminder;

    @Override
    public void execute(final JobExecutionContext context) {
        try {
            LOG.info("Sending email reminders of harvests which require harvest report...");
            Map<Long, Set<String>> emails = harvestReportReminder.sendReminders();
            LOG.info("Done, sent email reminders:{}", emails);

        } catch (final Exception e) {
            LOG.error("Error sending harvest report reminder emails", e);
        }
    }
}
