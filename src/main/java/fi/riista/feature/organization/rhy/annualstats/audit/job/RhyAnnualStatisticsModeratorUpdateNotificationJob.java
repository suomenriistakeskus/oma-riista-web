package fi.riista.feature.organization.rhy.annualstats.audit.job;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import fi.riista.feature.organization.rhy.annualstats.audit.RhyAnnualStatisticsNotificationFeature;
import io.sentry.Sentry;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "RhyAnnualStatisticsModeratorUpdateNotificationJob",
        enabledProperty = "rhy.annualstatistics.notification.moderatorupdate.enabled",
        cronExpression = "${rhy.annualstatistics.notification.moderatorupdate.schedule}")
public class RhyAnnualStatisticsModeratorUpdateNotificationJob extends RunAsAdminJob {

    private static final Logger LOG = LoggerFactory.getLogger(RhyAnnualStatisticsModeratorUpdateNotificationJob.class);

    @Resource
    private RhyAnnualStatisticsNotificationFeature notificationFeature;

    @Override
    protected void executeAsAdmin() {
        try {
            LOG.info("Starting ...");
            notificationFeature.sendModeratorUpdateNotifications();
            LOG.info("Done.");

        } catch (final Exception e) {
            LOG.error("RHY annual statistics moderator update notification job threw exception", e);
            Sentry.capture(e);
        }
    }
}
