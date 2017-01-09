package fi.riista.feature.gamediary.image;

import fi.riista.config.quartz.QuartzScheduledJob;
import org.joda.time.Hours;
import org.joda.time.ReadablePeriod;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "RemoveTemporaryGameDiaryImages",
        fixedRate = 3600 * 1000
)
public class RemoveTemporaryGameDiaryImagesJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(RemoveTemporaryGameDiaryImagesJob.class);

    private static final ReadablePeriod EXPIRATION_TIME = Hours.TWO;

    @Resource
    private TemporaryGameDiaryImagesRemover temporaryGameDiaryImagesRemover;

    @Override
    public void execute(JobExecutionContext context) {
        try {
            temporaryGameDiaryImagesRemover.removeExpiredTemporaryImages(EXPIRATION_TIME);
        } catch (Exception e) {
            LOG.error("Error removing expired temporary images", e);
        }
    }
}
