package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.APPROVED;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.IN_PROGRESS;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.UNDER_INSPECTION;

@Component
public class RhyAnnualStatisticsStateTransitionService {

    @Resource
    private RhyAnnualStatisticsStateChangeEventRepository stateChangeEventRepo;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void transitionToInProgress(final RhyAnnualStatistics annualStats) {
        assertState(annualStats.getState() == IN_PROGRESS,
                "RHY annual statistics is in incorrect state while adding in-progress state change event");

        addStateChangeEvent(annualStats, IN_PROGRESS);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void transitionToSubmittedForInspection(final RhyAnnualStatistics annualStats) {
        assertState(annualStats.isReadyForInspection(),
                "RHY annual statistics is not ready to be submitted for inspection");

        annualStats.setState(UNDER_INSPECTION);
        addStateChangeEvent(annualStats, UNDER_INSPECTION);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void transitionToApproved(final RhyAnnualStatistics annualStats) {
        assertState(annualStats.isCompleteForApproval(), "RHY annual statistics is not complete for approval");

        annualStats.setState(APPROVED);
        addStateChangeEvent(annualStats, APPROVED);
    }

    private void addStateChangeEvent(final RhyAnnualStatistics annualStats, final RhyAnnualStatisticsState state) {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        stateChangeEventRepo.save(new RhyAnnualStatisticsStateChangeEvent(annualStats, state, activeUser));
    }

    private static void assertState(final boolean expectedCondition, final String errorMessage) {
        if (!expectedCondition) {
            throw new IllegalRhyAnnualStatisticsStateTransitionException(errorMessage);
        }
    }
}
