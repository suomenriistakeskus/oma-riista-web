package fi.riista.integration.lupahallinta.job;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.integration.lupahallinta.HarvestPermitImportFeature;
import fi.riista.integration.lupahallinta.support.LupahallintaHarvestPermitImporter;
import fi.riista.integration.lupahallinta.support.LupahallintaHttpClient;
import fi.riista.integration.lupahallinta.support.LupahallintaImportMailHandler;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "LupahallintaHarvestPermitImport",
        enabledProperty = "lh.permit.import.enabled",
        group = "integration",
        cronExpression = "${lh.permit.import.schedule}"
)
public class LupahallintaHarvestPermitImportJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(LupahallintaHarvestPermitImportJob.class);

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HarvestPermitImportFeature harvestPermitImportFeature;

    @Resource
    private LupahallintaHttpClient lupahallintaHttpClient;

    @Resource
    private LupahallintaImportMailHandler mailHandler;

    @Override
    public void execute(JobExecutionContext context) {
        try {
            LOG.info("Import job starts.");

            activeUserService.loginWithoutCheck(createUser());

            final LupahallintaHarvestPermitImporter importer = new LupahallintaHarvestPermitImporter(
                    harvestPermitImportFeature, lupahallintaHttpClient, mailHandler);

            importer.doImport();

            LOG.info("Import job finished.");

        } catch (Exception e) {
            LOG.error("Error with import", e);

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private static SystemUser createUser() {
        final SystemUser u = new SystemUser() {
            @Override
            public String getHashedPassword() {
                return this.getClass().getSimpleName();
            }
        };
        u.setId(ActiveUserService.SCHEDULED_TASK_USER_ID);
        u.setUsername(LupahallintaHarvestPermitImportJob.class.getSimpleName());
        u.setRole(SystemUser.Role.ROLE_ADMIN);
        return u;
    }
}
