package fi.riista.feature.harvestpermit.report.autoapprove;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import org.quartz.DisallowConcurrentExecution;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "HarvestReportAutomaticApproval",
        enabledProperty = "harvest.autoapprove.enabled",
        cronExpression = "${harvest.autoapprove.schedule}"
)
public class HarvestReportAutomaticApprovalJob extends RunAsAdminJob {

    @Resource
    private HarvestReportAutomaticApprovalFeature harvestReportAutomaticApprovalFeature;

    @Override
    public void executeAsAdmin() {
        harvestReportAutomaticApprovalFeature.runAutoApprove();
    }
}
