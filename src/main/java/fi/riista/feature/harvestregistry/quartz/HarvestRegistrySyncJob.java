package fi.riista.feature.harvestregistry.quartz;

import fi.riista.config.quartz.QuartzScheduledJob;
import io.sentry.Sentry;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        enabledProperty = "harvestregistry.sync.enabled",
        name = "HarvestRegistrySync",
        cronExpression = "${harvestregistry.sync.schedule}"
)
public class HarvestRegistrySyncJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestRegistrySyncJob.class);

    @Resource
    private HarvestRegistrySynchronizerService harvestRegistrySynchronizerService;

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        LOG.info("Starting ...");

        try {
            harvestRegistrySynchronizerService.synchronize();
        } catch (Exception e) {
            LOG.error("Harvest registry synchronizing failed", e);
            Sentry.capture(e);
        }

        LOG.info("Done.");
    }
}
