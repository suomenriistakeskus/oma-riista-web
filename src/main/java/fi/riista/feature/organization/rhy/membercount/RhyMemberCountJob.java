package fi.riista.feature.organization.rhy.membercount;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "RhyMemberCountJob",
        enabledProperty = "rhy.membercount.enabled",
        cronExpression = "${rhy.membercount.schedule}"
)
public class RhyMemberCountJob extends RunAsAdminJob {

    private static final Logger LOG = LoggerFactory.getLogger(RhyMemberCountJob.class);

    @Resource
    private RhyMemberCountFeature rhyMemberCountFeature;

    @Override
    protected void executeAsAdmin() {
        try {
            LOG.info("RHY member count update starting.");
            rhyMemberCountFeature.updateMemberCounts();

            LOG.info("Update completed.");
        } catch (final Exception e) {
            LOG.error("RHY member count failed.", e);
        }
    }
}
