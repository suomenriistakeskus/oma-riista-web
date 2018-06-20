package fi.riista.feature.permit.invoice.reminder;

import fi.riista.config.quartz.QuartzScheduledJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "PermitInvoiceReminderJob",
        enabledProperty = "permit.invoice.reminder.enabled",
        cronExpression = "${permit.invoice.reminder.schedule}"
)
public class PermitInvoiceReminderJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(PermitInvoiceReminderJob.class);

    @Resource
    private PermitInvoiceReminderResolver permitInvoiceReminderResolver;

    @Resource
    private PermitInvoiceReminderSender permitInvoiceReminderSender;

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) {
        final List<PermitInvoiceReminderDTO> reminderList = permitInvoiceReminderResolver.resolve();

        LOG.info("Sending email reminders of permit invoices...");

        for (final PermitInvoiceReminderDTO dto : reminderList) {
            try {
                LOG.info("Sending invoice reminder for permitId={} to {}", dto.getHarvestPermitId(), dto.getRecipientEmails());

                permitInvoiceReminderSender.sendReminder(dto);

            } catch (final Exception e) {
                LOG.error("Error sending permit invoice reminder email", e);
            }
        }

        LOG.info("Done sending email reminders");
    }
}
