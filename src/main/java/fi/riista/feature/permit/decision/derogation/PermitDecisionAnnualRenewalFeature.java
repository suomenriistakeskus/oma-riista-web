package fi.riista.feature.permit.decision.derogation;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitDTO;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.feature.permit.decision.publish.AnnualPermitRenewalNotificationService;
import fi.riista.feature.permit.decision.publish.HarvestPermitDecisionSynchronizer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Comparator;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Service
public class PermitDecisionAnnualRenewalFeature {

    @Resource
    private PermitDecisionRepository decisionRepository;

    @Resource
    private HarvestPermitDecisionSynchronizer synchronizer;

    @Resource
    private AnnualPermitRenewalNotificationService notificationService;

    @Resource
    private HarvestPermitRepository permitRepository;

    @Resource
    private ActiveUserService activeUserService;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public boolean isRenewable(final long decisionId) {
        final PermitDecision decision = requireNonNull(decisionRepository.getOne(decisionId));
        final SystemUser user = activeUserService.requireActiveUser();

        return decision.isHandler(user) &&
                decision.isAnnualUnprotectedBird() &&
                decision.getStatus() == DecisionStatus.PUBLISHED &&
                latestPermitHasFinished(decision);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(rollbackFor = IOException.class)
    public HarvestPermitDTO createNextAnnualPermit(final long decisionId, final int permitYear) {
        final PermitDecision decision = requireNonNull(decisionRepository.getOne(decisionId));

        checkArgument(decision.isHandler(activeUserService.requireActiveUser()),
                "Current user must be the handler of the decision");

        final HarvestPermitDTO permit = synchronizer.createNewAnnualPermit(decision, permitYear);

        notificationService.notifyPermitRenewal(permit.getId());

        return permit;
    }

    private Boolean latestPermitHasFinished(final PermitDecision decision) {
        return permitRepository.findByPermitDecision(decision).stream()
                .max(Comparator.comparing(HarvestPermit::getPermitYear))
                .map(HarvestPermit::getHarvestReportState)
                .map(reportState -> HarvestReportState.APPROVED.equals(reportState))
                .orElse(false);
    }
}
