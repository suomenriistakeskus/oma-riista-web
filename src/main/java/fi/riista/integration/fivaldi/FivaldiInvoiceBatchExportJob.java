package fi.riista.integration.fivaldi;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import io.sentry.Sentry;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "FivaldiInvoiceBatchExportJob",
        enabledProperty = "fivaldi.invoice.export.enabled",
        cronExpression = "${fivaldi.invoice.export.schedule}"
)
public class FivaldiInvoiceBatchExportJob extends RunAsAdminJob {

    private static final Logger LOG = LoggerFactory.getLogger(FivaldiInvoiceBatchExportJob.class);

    @Resource
    private FivaldiInvoiceBatchExportFeature feature;

    @Override
    protected void executeAsAdmin() {
        try {
            LOG.info("Starting ...");
            feature.createAndStoreFivaldiInvoiceBatchFile();
            LOG.info("Done.");
        } catch (final Exception e) {
            LOG.error("Fivaldi invoice batch export job threw exception", e);
            Sentry.capture(e);
        }
    }
}
