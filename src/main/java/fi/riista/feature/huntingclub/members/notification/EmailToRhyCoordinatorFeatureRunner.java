package fi.riista.feature.huntingclub.members.notification;

import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.integration.common.entity.Integration;
import fi.riista.integration.common.repository.IntegrationRepository;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Component
public class EmailToRhyCoordinatorFeatureRunner {

    @Resource
    private IntegrationRepository integrationRepository;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional
    public void process(final HuntingLeaderFinderService feature, final HuntingLeaderEmailSenderService mailSender) {
        final DateTime processingStart = DateTime.now();
        final DateTime latestRun = getLastRunTime();

        final int currentHuntingYear = DateUtil.huntingYear();
        final List<Occupation> changedLeaders = feature.findChangedLeaders(
                latestRun, processingStart, currentHuntingYear);
        mailSender.sendMails(changedLeaders);
        updateLastRunTime(processingStart);
    }


    private DateTime getLastRunTime() {
        return Optional.ofNullable(getIntegration().getLastRun())
                .orElse(DateTime.now().minusDays(1));
    }

    private void updateLastRunTime(DateTime now) {
        getIntegration().setLastRun(now);
    }

    private Integration getIntegration() {
        return integrationRepository.getOne(Integration.EMAIL_HUNTING_LEADERS_TO_RHY_COORDINATOR_ID);
    }
}
