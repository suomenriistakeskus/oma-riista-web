package fi.riista.integration.lupahallinta.job;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.integration.lupahallinta.club.LHHuntingClubBatchConfig;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.JobOperator;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "LupahallintaHarvestClubImport",
        enabledProperty = "lh.club.import.enabled",
        group = "integration",
        cronExpression = "${lh.club.import.schedule}"
)
public class LupahallintaClubImportJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(LupahallintaClubImportJob.class);

    @Resource
    private JobOperator jobOperator;

    @Override
    public void execute(final JobExecutionContext context) {
        try {
            jobOperator.startNextInstance(LHHuntingClubBatchConfig.JOB_NAME);

        } catch (Exception e) {
            LOG.error("Error with import", e);
        }
    }
}
