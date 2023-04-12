package fi.riista.feature.permit.decision.publish;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Set;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "PermitDecisionPublishingJob",
        enabledProperty = "permit.decision.publishing.enabled",
        cronExpression = "${permit.decision.publishing.schedule}"
)
public class PermitDecisionPublishingJob extends RunAsAdminJob {

    private static final Logger LOG = LoggerFactory.getLogger(PermitDecisionPublishingJob.class);

    @Resource
    private PermitDecisionPublishingFeature permitDecisionPublishingFeature;

    @Override
    public void executeAsAdmin() {
        LOG.info("Starting ...");

        final Set<Long> revisionIds = permitDecisionPublishingFeature.findDecisionRevisionsToPublish();

        for (final long revisionId : revisionIds) {
            try {
                permitDecisionPublishingFeature.publishRevision(revisionId);
            } catch (Exception e) {
                LOG.error("Failed to publish decision revision id: " + revisionId, e);
            }
        }

        LOG.info("Done.");
    }

}
