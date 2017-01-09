package fi.riista.feature.huntingclub.members.notification;

import fi.riista.config.quartz.QuartzScheduledJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "EmailHuntingLeadersToRhyCoordinator",
        enabledProperty = "email.hunting.leaders.to.rhy.coordinator.enabled",
        cronExpression = "${email.hunting.leaders.to.rhy.coordinator.schedule}"
)
public class EmailToRhyCoordinatorJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(EmailToRhyCoordinatorJob.class);

    @Resource
    private EmailToRhyCoordinatorFeatureRunner runner;

    @Resource
    private HuntingLeaderFinderService feature;

    @Resource
    private HuntingLeaderEmailSenderService mailSender;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            LOG.info("Starting ...");
            runner.process(feature, mailSender);
            LOG.info("Done.");
        } catch (Exception e) {
            LOG.error("Processing threw exception", e);
        }
    }
}
