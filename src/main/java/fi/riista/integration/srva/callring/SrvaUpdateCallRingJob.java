package fi.riista.integration.srva.callring;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "SrvaUpdateCallRing",
        enabledProperty = "srva.callring.sync.enabled",
        cronExpression = "${srva.callring.sync.schedule}"
)
public class SrvaUpdateCallRingJob extends RunAsAdminJob {
    private static final Logger LOG = LoggerFactory.getLogger(SrvaUpdateCallRingJob.class);

    @Resource
    private SrvaUpdateCallRingFeature srvaUpdateCallRingFeature;

    @Override
    public void executeAsAdmin() {
        try {
            srvaUpdateCallRingFeature.configureAll();
        } catch (final Exception ex) {
            LOG.error("SRVA callRing sync has failed", ex);
        }
    }
}
