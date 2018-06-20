package fi.riista.integration.lupahallinta.job;

import fi.riista.config.quartz.RunAsAdminJob;
import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.integration.lupahallinta.HarvestPermitImportFeature;
import fi.riista.integration.lupahallinta.support.LupahallintaHarvestPermitImporter;
import fi.riista.integration.lupahallinta.support.LupahallintaHttpClient;
import fi.riista.integration.lupahallinta.support.LupahallintaPermitImportMailHandler;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "LupahallintaHarvestPermitImport",
        enabledProperty = "lh.permit.import.enabled",
        group = "integration",
        cronExpression = "${lh.permit.import.schedule}"
)
public class LupahallintaHarvestPermitImportJob extends RunAsAdminJob {

    private static final Logger LOG = LoggerFactory.getLogger(LupahallintaHarvestPermitImportJob.class);

    @Resource
    private HarvestPermitImportFeature harvestPermitImportFeature;

    @Resource
    private LupahallintaHttpClient lupahallintaHttpClient;

    @Resource
    private LupahallintaPermitImportMailHandler mailHandler;

    @Override
    public void executeAsAdmin() {
        try {
            LOG.info("Import job starts.");

            final LupahallintaHarvestPermitImporter importer = new LupahallintaHarvestPermitImporter(
                    harvestPermitImportFeature, lupahallintaHttpClient, mailHandler);

            importer.doImport();

            LOG.info("Import job finished.");

        } catch (Exception e) {
            LOG.error("Error with import", e);
        }
    }
}
