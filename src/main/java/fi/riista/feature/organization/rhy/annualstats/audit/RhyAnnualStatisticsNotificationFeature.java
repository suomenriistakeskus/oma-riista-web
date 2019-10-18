package fi.riista.feature.organization.rhy.annualstats.audit;

import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.integration.common.entity.Integration;
import fi.riista.integration.common.repository.IntegrationRepository;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static fi.riista.integration.common.entity.Integration.RHY_ANNUAL_STATISTICS_MODERATOR_UPDATE_NOTIFICATION_ID;
import static fi.riista.util.DateUtil.currentYear;

@Service
public class RhyAnnualStatisticsNotificationFeature {

    private static final Logger LOG = LoggerFactory.getLogger(RhyAnnualStatisticsNotificationFeature.class);

    @Resource
    private IntegrationRepository integrationRepository;

    @Resource
    private RhyAnnualStatisticsNotificationService notificationService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public void sendModeratorUpdateNotifications() {
        final DateTime currentTime = DateUtil.now();

        final Integration integration = getIntegrationForModeratorUpdateNotification();

        final DateTime lastRunOrBeginOfCurrentYear = Optional
                .ofNullable(integration.getLastRun())
                .orElseGet(() -> DateUtil.toDateTimeNullSafe(new LocalDate(currentYear(), 1, 1)));

        final Interval interval = new Interval(lastRunOrBeginOfCurrentYear, currentTime);

        final List<AggregatedAnnualStatisticsModeratorUpdateDTO> aggregatedModeratorUpdates =
                notificationService.findAnnualStatisticGroupsUpdatedByModerator(interval);

        LOG.info("Found {} annual statistics moderator update notifications to be sent from interval {}.",
                aggregatedModeratorUpdates.size(), interval);

        notificationService.sendModeratorUpdateNotifications(aggregatedModeratorUpdates);

        final List<RhyAnnualStatistics> approvedStatistics = notificationService.findApprovedAnnualStatistics(interval);

        LOG.info("Found {} annual statistics approved within interval {}.", approvedStatistics.size(), interval);

        notificationService.sendApprovalNotifications(approvedStatistics);

        integration.setLastRun(currentTime);
    }

    private Integration getIntegrationForModeratorUpdateNotification() {
        return Optional
                .ofNullable(integrationRepository.findOne(RHY_ANNUAL_STATISTICS_MODERATOR_UPDATE_NOTIFICATION_ID))
                .orElseGet(() -> {
                    final Integration newIntegration = new Integration();
                    newIntegration.setId(RHY_ANNUAL_STATISTICS_MODERATOR_UPDATE_NOTIFICATION_ID);
                    return integrationRepository.save(newIntegration);
                });
    }
}
