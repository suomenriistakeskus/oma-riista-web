package fi.riista.feature.permit.application.conflict;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import org.quartz.DisallowConcurrentExecution;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "ProcessHarvestPermitApplicationJob",
        enabledProperty = "process.harvest.permit.applications.enabled",
        cronExpression = "${process.harvest.permit.applications.schedule}")
public class SearchApplicationConflictsJob extends RunAsAdminJob {
    @Resource
    private SearchApplicationConflictsRunner processHarvestPermitApplicationRunner;

    @Override
    public void executeAsAdmin() {
        processHarvestPermitApplicationRunner.run();
    }
}
