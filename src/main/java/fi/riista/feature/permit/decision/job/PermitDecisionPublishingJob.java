package fi.riista.feature.permit.decision.job;

import com.newrelic.api.agent.NewRelic;
import fi.riista.config.quartz.QuartzScheduledJob;
import io.sentry.Sentry;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
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
public class PermitDecisionPublishingJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(PermitDecisionPublishingJob.class);

    @Resource
    private PermitDecisionPublishingFeature permitDecisionPublishingFeature;

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) {
        try {
            LOG.info("Starting ...");
            run();
            LOG.info("Done.");
        } catch (final Exception e) {
            LOG.error("Decision publising threw exception", e);
            NewRelic.noticeError(e, false);
            Sentry.capture(e);
        }
    }

    private void run() {
        final Set<Long> revisionIds = permitDecisionPublishingFeature.findDecisionRevisionsToGenerateHarvestPermits();

        for (final Long revisionId : revisionIds) {
            try {
                permitDecisionPublishingFeature.publishRevision(revisionId);
            } catch (Exception e) {
                LOG.error("Publishing revision id:" + revisionId + " failed.", e);
                NewRelic.noticeError(e, false);
                Sentry.capture(e);
            }
        }
    }
}
