package fi.riista.integration.metsastajarekisteri.shootingtest;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

import static fi.riista.util.DateUtil.today;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "ShootingTestExport",
        enabledProperty = "shootingtest.export.enabled",
        group = "integration",
        cronExpression = "${shootingtest.export.schedule}")
public class ShootingTestExportJob extends RunAsAdminJob {

    private static final Logger LOG = LoggerFactory.getLogger(ShootingTestExportJob.class);

    @Resource
    private ShootingTestExportFeature exportFeature;

    @Override
    protected void executeAsAdmin() {
        try {
            LOG.info("Shooting test export dataset creation job starts.");

            exportFeature.constructAndStoreShootingTestRegistry(today().minusDays(1));

            LOG.info("Shooting test export dataset creation job finished.");

        } catch (final Exception e) {
            LOG.error("Error while constructing dataset for shooting test export", e);
        }
    }
}
