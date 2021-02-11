package fi.riista.feature.harvestpermit.report;

import fi.riista.feature.account.pilot.DeerPilotRepository;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.HarvestChangeHistory;
import fi.riista.feature.gamediary.HarvestChangeHistoryRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestFieldValidator;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenValidator;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Component
public class HarvestReportModeratorService {

    @Resource
    private GISQueryService gisQueryService;

    @Resource
    private HarvestChangeHistoryRepository harvestChangeHistoryRepository;

    @Resource
    private DeerPilotRepository deerPilotRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void changeHarvestReportState(final HarvestReportStateChangeDTO dto,
                                         final SystemUser activeUser,
                                         final Harvest harvest) {

        DtoUtil.assertNoVersionConflict(harvest, dto.getRev());

        if (harvest.getHarvestReportState() == null) {
            throw new IllegalArgumentException("Harvest report is not done");
        }

        final HarvestPermit permit = harvest.getHarvestPermit();
        assertPermitHarvestReportNotApproved(permit);

        validate(harvest, resolveHarvestSpecVersion(isDeerPilotEnabled(harvest)));

        final HarvestReportState reportState = dto.getTo();
        harvest.setHarvestReportState(reportState);

        createHarvestChangeEvent(harvest, activeUser, reportState, dto.getReason());

        if (reportState == HarvestReportState.APPROVED) {
            if (permit != null && !harvest.isAcceptedToHarvestPermit()) {
                harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
            }
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void approvePermitHarvestReportsInBulk(final SystemUser activeUser, final HarvestPermit permit) {
        assertPermitHarvestReportNotApproved(permit);

        final HarvestSpecVersion specVersion = resolveHarvestSpecVersion(isDeerPilotEnabled(permit));

        permit.getHarvests().stream()
                .filter(HasHarvestReportState::isHarvestReportSentForApproval)
                .forEach(harvest -> {
                    validate(harvest, specVersion);
                    harvest.setHarvestReportState(HarvestReportState.APPROVED);
                    createHarvestChangeEvent(harvest, activeUser, HarvestReportState.APPROVED, null);
                });
    }

    // TODO Remove this when deer pilot 2020 is over.
    private HarvestSpecVersion resolveHarvestSpecVersion(final boolean isDeerPilotEnabled) {
        return HarvestSpecVersion.CURRENTLY_SUPPORTED.revertIfNotOnDeerPilot(isDeerPilotEnabled);
    }

    // TODO Remove this when deer pilot 2020 is over.
    private boolean isDeerPilotEnabled(final Harvest harvest) {
        return deerPilotRepository.isPersonInPilotGroup(harvest.getAuthor());
    }

    // TODO Remove this when deer pilot 2020 is over.
    private boolean isDeerPilotEnabled(final HarvestPermit permit) {
        return deerPilotRepository.findByHarvestPermitId(permit.getId()).isPresent();
    }

    private static void assertPermitHarvestReportNotApproved(final HarvestPermit permit) {
        if (permit != null && permit.isHarvestReportApproved()) {
            throw new IllegalArgumentException("Permit harvest report is approved");
        }
    }

    private static void validate(final Harvest harvest, final HarvestSpecVersion specVersion) {
        final int gameSpeciesCode = harvest.getSpecies().getOfficialCode();
        final int huntingYear = DateUtil.huntingYearContaining(harvest.getPointOfTimeAsLocalDate());
        final HarvestReportingType reportingType = harvest.resolveReportingType();

        // Use false as deer pilot user since bulk save will not fill the voluntary fields
        final RequiredHarvestFields.Report requirements =
                RequiredHarvestFields.getFormFields(huntingYear, gameSpeciesCode, reportingType, false, false);
        new HarvestFieldValidator(requirements, harvest).validateAll().throwOnErrors();

        final RequiredHarvestFields.Specimen specimenFieldRequirements = RequiredHarvestFields.getSpecimenFields(
                huntingYear, gameSpeciesCode, harvest.getHuntingMethod(), reportingType, false, specVersion);

        for (final HarvestSpecimen harvestSpecimen : harvest.getSortedSpecimens()) {
            new HarvestSpecimenValidator(specimenFieldRequirements, harvestSpecimen, gameSpeciesCode, false, false)
                    .validateAll()
                    .throwOnErrors();
        }
    }

    private void createHarvestChangeEvent(final Harvest harvest,
                                          final SystemUser activeUser,
                                          final HarvestReportState toState,
                                          final String reason) {
        final HarvestChangeHistory harvestChangeHistory = new HarvestChangeHistory();
        harvestChangeHistory.setHarvest(harvest);
        harvestChangeHistory.setPointOfTime(DateUtil.now());
        harvestChangeHistory.setUserId(activeUser.getId());
        harvestChangeHistory.setHarvestReportState(toState);

        if (toState.stateChangeRequiresReasonFromModerator() && !StringUtils.hasText(reason)) {
            throw new IllegalArgumentException("Reason is required for moderator/admin");
        }

        harvestChangeHistory.setReasonForChange(reason);

        if (toState.requiresPropertyIdentifier()) {
            gisQueryService.findPropertyByLocation(harvest.getGeoLocation())
                    .map(MMLRekisteriyksikonTietoja::getPropertyIdentifier)
                    .ifPresent(harvest::setPropertyIdentifier);
        }

        harvestChangeHistoryRepository.save(harvestChangeHistory);
    }
}
