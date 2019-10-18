package fi.riista.feature.harvestpermit.endofhunting;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.CommitHookService;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformer;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitAuthorization;
import fi.riista.feature.harvestpermit.report.HarvestReportModeratorService;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.report.email.HarvestReportNotificationService;
import fi.riista.feature.harvestpermit.statistics.HarvestPermitUsageDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.DtoUtil;
import org.joda.time.DateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Component
public class EndOfHuntingHarvestReportFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private CommitHookService commitHookService;

    @Resource
    private HarvestReportNotificationService harvestReportNotificationService;

    @Resource
    private HarvestDTOTransformer harvestDTOTransformer;

    @Resource
    private HarvestReportModeratorService harvestReportModeratorService;

    @Transactional(readOnly = true)
    public EndOfHuntingHarvestReportDTO getEndOfHuntingReport(final long permitId) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        if (harvestPermit.isMooselikePermitType() || harvestPermit.isAmendmentPermit()) {
            throw new IllegalStateException("This report is not used for permit type " + harvestPermit.getPermitTypeCode());
        }
        return getEndOfHuntingReport(harvestPermit);
    }

    @Nonnull
    private EndOfHuntingHarvestReportDTO getEndOfHuntingReport(final HarvestPermit harvestPermit) {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final List<Harvest> acceptedHarvests = harvestPermit.getAcceptedHarvestForEndOfHuntingReport();

        final List<HarvestDTO> harvestDTOs = harvestDTOTransformer.apply(acceptedHarvests);
        final List<HarvestPermitUsageDTO> usage = HarvestPermitUsageDTO.createUsage(
                harvestPermit.getSpeciesAmounts(), acceptedHarvests);

        return new EndOfHuntingHarvestReportDTO(harvestPermit, activeUser, usage, harvestDTOs);
    }

    @Transactional
    public EndOfHuntingHarvestReportDTO createEndOfHuntingReport(final long permitId) {
        return createEndOfHuntingReport(permitId, null);
    }

    @Transactional
    public EndOfHuntingHarvestReportDTO createEndOfHuntingReport(final long permitId,
                                                                 final EndOfHuntingReportModeratorCommentsDTO endOfHuntingReportComments) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(permitId,
                HarvestPermitAuthorization.Permission.CREATE_REMOVE_HARVEST_REPORT);

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

        return getEndOfHuntingReport(harvestPermit);
    }

    @Nonnull
    private static Person getEndOfHuntingReportAuthor(final SystemUser activeUser, final HarvestPermit permit) {
        if (activeUser.isModeratorOrAdmin()) {
            return permit.getOriginalContactPerson();
        } else {
            return Objects.requireNonNull(activeUser.getPerson(), "active person is missing");
        }
    }

    @Transactional
    public void deleteEndOfHuntingReport(final long permitId) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(permitId,
                HarvestPermitAuthorization.Permission.CREATE_REMOVE_HARVEST_REPORT);

        final SystemUser activeUser = activeUserService.requireActiveUser();
        if (!activeUser.isModeratorOrAdmin() && harvestPermit.isHarvestReportApproved()) {
            throw new IllegalStateException("End of hunting report cannot be deleted");
        }

        harvestPermit.setHarvestReportState(null);
        harvestPermit.setHarvestReportDate(null);
        harvestPermit.setHarvestReportAuthor(null);
        harvestPermit.setHarvestReportModeratorOverride(null);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public void changeEndOfHuntingHarvestReportState(final EndOfHuntingHarvestReportStateChangeDTO dto) {
        final SystemUser activeUser = activeUserService.requireActiveUser();

        if (!activeUser.isModeratorOrAdmin()) {
            throw new IllegalStateException();
        }

        final HarvestPermit permit = requireEntityService.requireHarvestPermit(dto.getId(), EntityPermission.UPDATE);
        DtoUtil.assertNoVersionConflict(permit, dto.getRev());

        if (!permit.isHarvestReportDone()) {
            throw new IllegalArgumentException("Harvest report is not done");
        }

        if (dto.getTo() == HarvestReportState.APPROVED) {
            harvestReportModeratorService.approvePermitHarvestReportsInBulk(activeUser, permit);
            permit.setEndOfHuntingReportComments(dto.getEndOfHuntingReportComments().getEndOfHuntingReportComments());
        }

        permit.setHarvestReportState(dto.getTo());
    }
}
