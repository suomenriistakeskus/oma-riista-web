package fi.riista.integration.metsahallitus.permit;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "MetsahallitusPermitDeleteJob",
        enabledProperty = "delete.metsahallitus.permit.enabled",
        cronExpression = "${delete.metsahallitus.permit.schedule}"
)
public class MetsahallitusPermitDeleteJob extends RunAsAdminJob {
    private static final Logger LOG = LoggerFactory.getLogger(MetsahallitusPermitDeleteJob.class);

    @Resource
    private MetsahallitusPermitDeleteFeature metsahallitusPermitDeleteFeature;

    @Override
    public void executeAsAdmin() {
        try {
            LOG.info("Deleting old Metsahallitus permits.");
            final long result = metsahallitusPermitDeleteFeature.deleteOldPermits();
            LOG.info("Deleted {} old permits", result);
        } catch (Exception e) {
            LOG.error("Metsahallitus permits processing threw exception", e);
        }
    }

}
