package fi.riista.feature.organization.rhy.annualstats.statechange;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.APPROVED;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.IN_PROGRESS;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.NOT_STARTED;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.UNDER_INSPECTION;

@Component
public class RhyAnnualStatisticsStateTransitionService {

    @Resource
    private RhyAnnualStatisticsStateChangeEventRepository stateChangeEventRepo;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private EnumLocaliser localiser;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void transitionToNotStarted(final RhyAnnualStatistics annualStats) {
        assertState(annualStats.getState() == NOT_STARTED,
                "RHY annual statistics is in incorrect state while adding not-started state change event");

        addStateChangeEvent(annualStats, NOT_STARTED);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void transitionToInProgress(final RhyAnnualStatistics annualStats) {
        assertState(annualStats.getState() == NOT_STARTED,
                "RHY annual statistics is in incorrect state while adding in-progress state change event");

        annualStats.setState(IN_PROGRESS);
        addStateChangeEvent(annualStats, IN_PROGRESS);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void transitionToSubmittedForInspection(final RhyAnnualStatistics annualStats) {
        annualStats.assertIsUpdateable(activeUserService.isModeratorOrAdmin(), localiser);

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

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void cancelApproval(final RhyAnnualStatistics annualStats) {
        assertState(annualStats.getState() == APPROVED, "RHY annual statistics is not approved");

        annualStats.setState(UNDER_INSPECTION);
        addStateChangeEvent(annualStats, UNDER_INSPECTION);
    }

    private void addStateChangeEvent(final RhyAnnualStatistics annualStats, final RhyAnnualStatisticsState state) {
        stateChangeEventRepo.save(new RhyAnnualStatisticsStateChangeEvent(annualStats, state));
    }

    private static void assertState(final boolean expectedCondition, final String errorMessage) {
        if (!expectedCondition) {
            throw new IllegalRhyAnnualStatisticsStateTransitionException(errorMessage);
        }
    }
}
