package fi.riista.feature.gamediary.harvest.mutation;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.HarvestChangeHistory;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTOBase;
import fi.riista.feature.gamediary.harvest.HarvestFieldValidator;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HarvestReportingTypeTransition;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersionNotSupportedException;
import fi.riista.feature.gamediary.harvest.HuntingMethod;
import fi.riista.feature.gamediary.harvest.PermittedMethod;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.feature.gamediary.harvest.mutation.basic.HarvestAuthorActorMutation;
import fi.riista.feature.gamediary.harvest.mutation.basic.HarvestCommonMutation;
import fi.riista.feature.gamediary.harvest.mutation.basic.HarvestDeerHuntingMutation;
import fi.riista.feature.gamediary.harvest.mutation.basic.HarvestGISMutation;
import fi.riista.feature.gamediary.harvest.mutation.basic.HarvestLocationMutation;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestChangeReasonRequiredException;
import fi.riista.feature.gamediary.harvest.mutation.report.HarvestCreateReportMutation;
import fi.riista.feature.gamediary.harvest.mutation.report.HarvestDeleteReportMutation;
import fi.riista.feature.gamediary.harvest.mutation.report.HarvestForDiaryMutation;
import fi.riista.feature.gamediary.harvest.mutation.report.HarvestForPermitMutation;
import fi.riista.feature.gamediary.harvest.mutation.report.HarvestMutationForReportType;
import fi.riista.feature.gamediary.harvest.mutation.report.HarvestUpdateReportMutation;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class HarvestUpdater {

    public static HarvestUpdater create(@Nonnull final HarvestMutationFactory harvestMutationFactory,
                                        @Nonnull final Harvest harvestBeforeUpdate,
                                        @Nonnull final SystemUser activeUser,
                                        final boolean isDeerPilotActive) {

        final HarvestPreviousState previousState = new HarvestPreviousState(harvestBeforeUpdate);
        final HarvestMutationRole mutationRole =
                HarvestMutationRole.getMutationRoleForHarvest(harvestBeforeUpdate, activeUser);

        return new HarvestUpdater(harvestMutationFactory, previousState, mutationRole, activeUser, isDeerPilotActive);
    }

    private final HarvestMutationFactory harvestMutationFactory;
    private final HarvestPreviousState previousState;

    private final Person activePerson;
    private final Long activeUserId;
    private final HarvestMutationRole mutationRole;
    private final boolean deerPilotActive;
    private final List<HarvestMutation> mutators = new LinkedList<>();

    private HarvestUpdater(@Nonnull final HarvestMutationFactory harvestMutationFactory,
                           @Nonnull final HarvestPreviousState previousState,
                           @Nonnull final HarvestMutationRole mutationRole,
                           @Nonnull final SystemUser activeUser,
                           final boolean isDeerPilotActive) {

        this.harvestMutationFactory = requireNonNull(harvestMutationFactory);
        this.previousState = requireNonNull(previousState);
        this.activePerson = requireNonNull(activeUser).getPerson();
        this.activeUserId = activeUser.getId();
        this.mutationRole = mutationRole;
        this.deerPilotActive = isDeerPilotActive;
    }

    private void clearMutations() {
        this.mutators.clear();
    }

    private void addMutation(final HarvestMutation mutation) {
        this.mutators.add(requireNonNull(mutation));
    }

    public HarvestReportingType execute(final Harvest harvest, final HarvestDTO dto) {
        final HarvestReportingType reportingType = prepare(dto);
        checkPreConditions(reportingType, dto.getHarvestSpecVersion(), false);
        executeUpdate(harvest);
        checkPostConditions(reportingType, harvest, deerPilotActive);
        return reportingType;
    }

    public HarvestReportingType execute(final Harvest harvest, final MobileHarvestDTO dto) {
        final HarvestReportingType reportingType = prepare(dto);
        checkPreConditions(reportingType, dto.getHarvestSpecVersion(), true);
        executeUpdate(harvest);
        checkPostConditions(reportingType, harvest, deerPilotActive);
        return reportingType;
    }

    private void addHarvestReportMutation(final HarvestMutationForReportType reportingTypeMutation) {
        if (reportingTypeMutation.isHarvestReportRequired()) {
            addMutation(previousState.isHarvestReportDone()
                    ? new HarvestUpdateReportMutation(mutationRole, previousState)
                    : new HarvestCreateReportMutation(mutationRole, activePerson));

        } else if (previousState.isHarvestReportDone()) {
            addMutation(new HarvestDeleteReportMutation(mutationRole));
        }
    }

    private HarvestMutationForReportType createMutationForReportingType(final HarvestDTOBase dto,
                                                                        final boolean supportsPermittedMethod,
                                                                        final PermittedMethod permittedMethod,
                                                                        final HuntingMethod huntingMethod,
                                                                        final HarvestCommonMutation commonMutation,
                                                                        final HarvestLocationMutation locationMutation,
                                                                        final HarvestGISMutation gisMutation) {
        // Ignore all permits and report fields before support begins
        if (commonMutation.getHarvestDate().isBefore(Harvest.REPORT_REQUIRED_SINCE)) {
            return new HarvestForDiaryMutation(false);
        }

        if (dto.hasPermitNumber()) {
            final HarvestForPermitMutation permitMutation = harvestMutationFactory.createPermitMutation(
                    mutationRole, activePerson, commonMutation, locationMutation, gisMutation,
                    supportsPermittedMethod, dto.getHarvestSpecVersion(), dto.getPermitNumber(), permittedMethod,
                    huntingMethod);

            if (permitMutation == null) {
                // Ignore moose-like permit
                return new HarvestForDiaryMutation(false);
            }

            return permitMutation;
        }

        return harvestMutationFactory.createSeasonMutation(mutationRole, dto, commonMutation, gisMutation);
    }

    private HarvestReportingType prepare(final HarvestDTO dto) {
        // Try to do queries first (as far as possible) in order to prevent harvest revision bumping many integer steps.
        final HarvestLocationMutation locationMutation =
                HarvestLocationMutation.createForWeb(dto, previousState.getPreviousLocation());
        final HarvestGISMutation gisMutation = harvestMutationFactory.createGISMutation(locationMutation);
        final HarvestCommonMutation commonMutation = harvestMutationFactory.createCommonMutation(mutationRole, dto);
        final HarvestAuthorActorMutation authorActorMutation =
                harvestMutationFactory.createAuthorActorMutation(mutationRole, activePerson, dto, previousState);
        final HarvestDeerHuntingMutation deerHuntingMutation = harvestMutationFactory.createDeerHuntingMutation(dto);

        final HarvestMutationForReportType reportingTypeMutation = dto.getHuntingDayId() != null
                ? harvestMutationFactory.createHuntingDayMutation(dto, activePerson)
                : createMutationForReportingType(dto, true, dto.getPermittedMethod(), dto.getHuntingMethod(), commonMutation, locationMutation, gisMutation);


        clearMutations();
        addMutation(locationMutation);
        addMutation(gisMutation);
        addMutation(commonMutation);
        addMutation(authorActorMutation);
        addMutation(reportingTypeMutation);
        addMutation(deerHuntingMutation);
        addHarvestReportMutation(reportingTypeMutation);

        return reportingTypeMutation.getReportingType();
    }

    // TODO: Add support for permitted fields
    private HarvestReportingType prepare(final MobileHarvestDTO dto) {
        final HarvestLocationMutation locationMutation =
                HarvestLocationMutation.createForMobile(dto, previousState.getPreviousLocation());
        final HarvestGISMutation gisMutation = harvestMutationFactory.createGISMutation(locationMutation);
        final HarvestCommonMutation commonMutation = harvestMutationFactory.createCommonMutation(mutationRole, dto);
        final HarvestDeerHuntingMutation deerHuntingMutation = harvestMutationFactory.createDeerHuntingMutation(dto);

        final HarvestMutationForReportType reportingTypeMutation =
                createMutationForReportingType(dto, false, null, null, commonMutation, locationMutation, gisMutation);

        clearMutations();
        addMutation(locationMutation);
        addMutation(gisMutation);
        addMutation(commonMutation);
        addMutation(reportingTypeMutation);
        addMutation(deerHuntingMutation);
        addHarvestReportMutation(reportingTypeMutation);

        return reportingTypeMutation.getReportingType();
    }

    private void checkPreConditions(final HarvestReportingType reportingType,
                                    final HarvestSpecVersion specVersion,
                                    final boolean mobileClient) {

        assertSupport(reportingType, specVersion, mobileClient);

        final HarvestReportingType previousReportingType = previousState.getPreviousReportingType();

        reportingType.assertValidReportingType(previousReportingType, mutationRole);

        if (previousReportingType != null) {
            assertSupport(previousReportingType, specVersion, mobileClient);

            new HarvestReportingTypeTransition(previousReportingType, reportingType, mutationRole)
                    .assertValidTransition();
        }
    }

    private static void assertSupport(@Nullable final HarvestReportingType reportingType,
                                      final HarvestSpecVersion specVersion,
                                      final boolean mobileClient) {

        if (reportingType == HarvestReportingType.HUNTING_DAY && mobileClient) {
            throw HarvestSpecVersionNotSupportedException.groupHuntingNotSupported(specVersion);
        }

        if (reportingType == HarvestReportingType.SEASON && !specVersion.supportsHarvestReport()) {
            throw HarvestSpecVersionNotSupportedException.seasonNotSupported(specVersion);
        }
    }

    private void executeUpdate(final Harvest harvest) {
        for (final Consumer<Harvest> mutator : mutators) {
            mutator.accept(harvest);
        }
        clearMutations();
    }

    private static void checkPostConditions(final HarvestReportingType reportingType,
                                            final Harvest harvest,
                                            final boolean isDeerPilotEnabled) {
        // Validate updated fields
        final RequiredHarvestFields.Report reportRequirements = RequiredHarvestFields.getFormFields(
                DateUtil.huntingYearContaining(harvest.getPointOfTimeAsLocalDate()),
                harvest.getSpecies().getOfficialCode(), reportingType, false, isDeerPilotEnabled);

        new HarvestFieldValidator(reportRequirements, harvest).validateAll().throwOnErrors();
    }

    public boolean shouldSendHarvestNotificationEmail(final Harvest harvest) {
        if (!harvest.isHarvestReportDone()) {
            // Notification is only sent when harvest report exists
            return false;
        }

        if (harvest.getHarvestPermit() != null && harvest.getHarvestPermit().isHarvestsAsList()) {
            // Notification is not sent before end of hunting report is created
            return false;
        }

        // Report was updated, skip notification if basic details have not changed
        return !previousState.isHarvestReportDone() || previousState.shouldInitReportState(harvest);
    }

    public HarvestChangeHistory createHistoryEventIfRequired(final SystemUser activeUser,
                                                             final Harvest harvest,
                                                             final HarvestReportingType reportingType,
                                                             final String moderatorReasonForChange) {

        if (activeUser.isModeratorOrAdmin() && reportingType != HarvestReportingType.HUNTING_DAY) {
            if (StringUtils.hasText(moderatorReasonForChange)) {
                return createHistoryEvent(harvest, activeUserId, moderatorReasonForChange);
            }

            throw new HarvestChangeReasonRequiredException(activeUser.getRole());
        }

        return createHistoryEventIfRequiredForPerson(harvest);
    }

    public HarvestChangeHistory createHistoryEventIfRequiredForPerson(final Harvest harvest) {
        return previousState.hasHarvestReportStateChanged(harvest)
                ? createHistoryEvent(harvest, activeUserId, null) : null;
    }

    private static HarvestChangeHistory createHistoryEvent(final Harvest harvest,
                                                           final Long activeUserId,
                                                           final String reason) {

        final HarvestChangeHistory historyEvent = new HarvestChangeHistory();
        historyEvent.setHarvest(harvest);
        historyEvent.setPointOfTime(DateUtil.now());
        historyEvent.setHarvestReportState(harvest.getHarvestReportState());
        historyEvent.setUserId(activeUserId);
        historyEvent.setReasonForChange(reason);
        return historyEvent;
    }
}
