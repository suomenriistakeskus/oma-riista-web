package fi.riista.feature.organization.jht.email;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "NotifyJhtOccupationNominationToRkaJob",
        enabledProperty = "email.notify.jht.occupation.nomination.to.rka.enabled",
        cronExpression = "${email.notify.jht.occupation.nomination.to.rka.schedule}"
)
public class NotifyJhtOccupationNominationToRkaJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(NotifyJhtOccupationNominationToRkaJob.class);

    @Resource
    private NotifyJhtOccupationNominationToRkaService notifyJhtOccupationNominationToRkaService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            final LocalDate yesterday = DateUtil.today().minusDays(1);

            LOG.info("Sending notifications for nominationDate {} ...", yesterday);

            notifyJhtOccupationNominationToRkaService.sendNotificationEmail(yesterday);

            LOG.info("Done.");

        } catch (Exception e) {
            LOG.error("Processing threw exception", e);
        }
    }
}
