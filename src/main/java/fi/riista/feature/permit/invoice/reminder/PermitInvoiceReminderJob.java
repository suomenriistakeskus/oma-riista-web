package fi.riista.feature.permit.invoice.reminder;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import org.quartz.DisallowConcurrentExecution;
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
public class PermitInvoiceReminderJob extends RunAsAdminJob {

    private static final Logger LOG = LoggerFactory.getLogger(PermitInvoiceReminderJob.class);

    @Resource
    private PermitInvoiceReminderResolver permitInvoiceReminderResolver;

    @Resource
    private PermitInvoiceReminderSender permitInvoiceReminderSender;

    @Override
    public void executeAsAdmin() {
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
