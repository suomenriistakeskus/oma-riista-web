package fi.riista.feature.harvestpermit.endofhunting;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.CommitHookService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportModeratorService;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.report.email.HarvestReportNotificationService;
import fi.riista.feature.organization.person.Person;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;

@Component
public class EndOfHuntingReportCreateService {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HarvestReportModeratorService harvestReportModeratorService;

    @Resource
    private CommitHookService commitHookService;

    @Resource
    private HarvestReportNotificationService harvestReportNotificationService;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void createEndOfHuntingReport(final HarvestPermit harvestPermit,
                                         final EndOfHuntingReportModeratorCommentsDTO endOfHuntingReportComments) {
        if (harvestPermit.isMooselikePermitType() || harvestPermit.isAmendmentPermit()) {
            throw new IllegalStateException("Can not crete report to permit which type is " + harvestPermit.getPermitTypeCode());
        }

        if (harvestPermit.isHarvestReportDone()) {
            throw new IllegalStateException("Harvest report already done for permit");
        }

        if (harvestPermit.hasHarvestProposedToPermit()) {
            throw new IllegalStateException("Can not create report to permit which has proposed harvests.");
        }

        final SystemUser activeUser = activeUserService.requireActiveUser();

        if (endOfHuntingReportComments != null && !activeUser.isModeratorOrAdmin()) {
            throw new IllegalArgumentException("Only moderators can add comments");
        }

        if (activeUser.isModeratorOrAdmin()) {
            harvestReportModeratorService.approvePermitHarvestReportsInBulk(activeUser, harvestPermit);
        }

        harvestPermit.setHarvestReportDate(DateTime.now());
        harvestPermit.setHarvestReportState(activeUser.isModeratorOrAdmin()
                ? HarvestReportState.APPROVED
                : HarvestReportState.SENT_FOR_APPROVAL);
        harvestPermit.setHarvestReportAuthor(getEndOfHuntingReportAuthor(activeUser, harvestPermit));
        harvestPermit.setHarvestReportModeratorOverride(activeUser.isModeratorOrAdmin());
        if (endOfHuntingReportComments != null) {
            harvestPermit.setEndOfHuntingReportComments(endOfHuntingReportComments.getEndOfHuntingReportComments());
        }

        commitHookService.runInTransactionAfterCommit(() -> {
            harvestReportNotificationService.sendNotificationForPermit(harvestPermit.getId());
        });
    }

    @Nonnull
    private static Person getEndOfHuntingReportAuthor(final SystemUser activeUser, final HarvestPermit permit) {
        if (activeUser.isModeratorOrAdmin()) {
            return permit.getOriginalContactPerson();
        } else {
            return Objects.requireNonNull(activeUser.getPerson(), "active person is missing");
        }
    }
}
