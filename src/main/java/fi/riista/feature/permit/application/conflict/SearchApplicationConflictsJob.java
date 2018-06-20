package fi.riista.feature.permit.application.conflict;

import fi.riista.config.quartz.QuartzScheduledJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "ProcessHarvestPermitApplicationJob",
        enabledProperty = "process.harvest.permit.applications.enabled",
        cronExpression = "${process.harvest.permit.applications.schedule}")
public class SearchApplicationConflictsJob implements Job {
    @Resource
    private SearchApplicationConflictsRunner processHarvestPermitApplicationRunner;

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) {
        processHarvestPermitApplicationRunner.run();
    }
}
