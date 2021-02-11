package fi.riista.feature.gamediary.image;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import org.joda.time.Hours;
import org.joda.time.ReadablePeriod;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "RemoveTemporaryGameDiaryImages",
        fixedRate = 3600 * 1000
)
public class RemoveTemporaryGameDiaryImagesJob extends RunAsAdminJob {

    private static final Logger LOG = LoggerFactory.getLogger(RemoveTemporaryGameDiaryImagesJob.class);

    private static final ReadablePeriod EXPIRATION_TIME = Hours.TWO;

    @Resource
    private TemporaryGameDiaryImagesRemover temporaryGameDiaryImagesRemover;

    @Override
    public void executeAsAdmin() {
        try {
            temporaryGameDiaryImagesRemover.removeExpiredTemporaryImages(EXPIRATION_TIME);
        } catch (Exception e) {
            LOG.error("Error removing expired temporary images", e);
        }
    }
}
