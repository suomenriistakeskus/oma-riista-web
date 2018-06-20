package fi.riista.feature.harvestpermit;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.CommitHookService;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestFieldValidator;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenValidator;
import fi.riista.feature.harvestpermit.endofhunting.EndOfHuntingReportExistsException;
import fi.riista.feature.harvestpermit.report.HarvestReportExistsException;
import fi.riista.feature.harvestpermit.report.HarvestReportNotSupportedException;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.report.email.HarvestReportNotificationService;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;

@Component
public class HarvestPermitAcceptHarvestFeature {

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HarvestReportNotificationService harvestReportNotificationService;

    @Resource
    private CommitHookService commitHookService;

    @Transactional
    public void changeAcceptedToPermit(@Nonnull HarvestPermitAcceptHarvestDTO dto) {
        Objects.requireNonNull(dto);

        final Harvest harvest = harvestRepository.getOne(dto.getHarvestId());
        final HarvestPermit harvestPermit = harvest.getHarvestPermit();
        DtoUtil.assertNoVersionConflict(harvest, dto.getHarvestRev());

        if (harvestPermit == null) {
            throw new IllegalArgumentException("Harvest does not have permit");
        }

        // must be permit contact person or moderator
        activeUserService.assertHasPermission(harvestPermit, HarvestPermitAuthorization.Permission.ACCEPT_REJECT_HARVEST);

        if (harvest.getStateAcceptedToHarvestPermit() == dto.getToState()) {
            // Nothing to do
            return;
        }

        if (harvest.isHarvestReportApproved() || harvest.isHarvestReportRejected()) {
            throw new HarvestReportExistsException(harvest);
        }

        if (harvestPermit.isHarvestReportDone()) {
            // end of hunting report must be removed first
            throw new EndOfHuntingReportExistsException(harvestPermit);
        }

        if (!harvestPermit.isHarvestReportAllowed()) {
            // moose permits are not supported
            throw new HarvestReportNotSupportedException(harvestPermit);
        }

        harvestPermit.forceRevisionUpdate();
        harvest.setStateAcceptedToHarvestPermit(dto.getToState());

        if (dto.getToState() == Harvest.StateAcceptedToHarvestPermit.ACCEPTED) {
            if (harvest.isHarvestReportDone()) {
                throw new HarvestReportExistsException(harvest);
            }

            validateHarvestFieldsForReporting(harvest);

            final SystemUser activeUser = activeUserService.requireActiveUser();
            harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            harvest.setHarvestReportDate(DateUtil.now());
            harvest.setHarvestReportAuthor(activeUser.isModeratorOrAdmin()
                    ? harvest.getHarvestPermit().getOriginalContactPerson()
                    : activeUser.getPerson());

            if (!harvestPermit.isHarvestsAsList()) {
                commitHookService.runInTransactionAfterCommit(() -> {
                    harvestReportNotificationService.sendNotificationForHarvest(harvest.getId());
                });
            }

        } else {
            harvest.setHarvestReportState(null);
            harvest.setHarvestReportAuthor(null);
            harvest.setHarvestReportDate(null);
        }
    }

    // Fields must be validated again because editing from mobile might cause inconsistencies
    private static void validateHarvestFieldsForReporting(final Harvest harvest) {
        final LocalDate harvestDate = DateUtil.toLocalDateNullSafe(harvest.getPointOfTime());
        final int huntingYear = DateUtil.huntingYearContaining(harvestDate);
        final int gameSpeciesCode = harvest.getSpecies().getOfficialCode();
        final boolean associatedWithHuntingDay = harvest.getHuntingDayOfGroup() != null;

        final HarvestReportingType reportingType = HarvestReportingType.PERMIT;
        final RequiredHarvestFields.Report reportFieldRequirements = RequiredHarvestFields.getFormFields(
                huntingYear, gameSpeciesCode, reportingType);
        final RequiredHarvestFields.Specimen specimenFieldRequirements = RequiredHarvestFields.getSpecimenFields(
                huntingYear, gameSpeciesCode, harvest.getHuntingMethod(), reportingType);

        new HarvestFieldValidator(reportFieldRequirements, harvest).validateAll().throwOnErrors();

        for (HarvestSpecimen harvestSpecimen : harvest.getSortedSpecimens()) {
            new HarvestSpecimenValidator(specimenFieldRequirements, harvestSpecimen,
                    gameSpeciesCode, associatedWithHuntingDay).validateAll().throwOnErrors();
        }
    }
}
