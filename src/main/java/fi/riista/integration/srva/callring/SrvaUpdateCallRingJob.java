package fi.riista.integration.srva.callring;

import fi.riista.config.quartz.QuartzScheduledJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "SrvaUpdateCallRing",
        enabledProperty = "srva.callring.sync.enabled",
        cronExpression = "${srva.callring.sync.schedule}"
)
public class SrvaUpdateCallRingJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(SrvaUpdateCallRingJob.class);

    @Resource
    private SrvaUpdateCallRingFeature srvaUpdateCallRingFeature;

    @Override
    public void execute(final JobExecutionContext context) {
        try {
            srvaUpdateCallRingFeature.configureAll();
        } catch (final Exception ex) {
            LOG.error("SRVA callRing sync has failed", ex);
        }
    }
}
