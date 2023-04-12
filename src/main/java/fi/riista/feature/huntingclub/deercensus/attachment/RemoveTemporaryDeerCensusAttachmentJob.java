package fi.riista.feature.huntingclub.deercensus.attachment;

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
        name = "RemoveTemporaryDeerCensusAttachments",
        fixedRate = 3600 * 1000
)
public class RemoveTemporaryDeerCensusAttachmentJob extends RunAsAdminJob {

    private static final Logger LOG = LoggerFactory.getLogger(RemoveTemporaryDeerCensusAttachmentJob.class);

    private static final ReadablePeriod EXPIRATION_TIME = Hours.TWO;

    @Resource
    private TemporaryDeerCensusAttachmentRemover temporaryDeerCensusAttachmentRemover;

    @Override
    public void executeAsAdmin() {
        try {
            temporaryDeerCensusAttachmentRemover.removeExpiredTemporaryImages(EXPIRATION_TIME);
        } catch (Exception e) {
            LOG.error("Error removing expired DeerCensusAttachments", e);
        }
    }
}
