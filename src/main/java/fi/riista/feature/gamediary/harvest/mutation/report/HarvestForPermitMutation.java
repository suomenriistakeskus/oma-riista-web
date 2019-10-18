package fi.riista.feature.gamediary.harvest.mutation.report;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersionNotSupportedException;
import fi.riista.feature.gamediary.harvest.HuntingMethod;
import fi.riista.feature.gamediary.harvest.PermittedMethod;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutationRole;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestPermitChangeForbiddenException;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountNotFound;
import fi.riista.feature.harvestpermit.endofhunting.EndOfHuntingReportExistsException;
import fi.riista.feature.organization.person.Person;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Objects;

public class HarvestForPermitMutation implements HarvestMutationForReportType {

    public static HarvestForPermitMutation create(@Nonnull final HarvestMutationRole mutationRole,
                                                  final Person activePerson,
                                                  @Nonnull final HarvestSpecVersion harvestSpecVersion,
                                                  final boolean supportsPermittedMethod,
                                                  @Nonnull final LocalDate harvestDate,
                                                  final int gameSpeciesCode,
                                                  @Nonnull final HarvestPermit harvestPermit,
                                                  final PermittedMethod permittedMethod,
                                                  final HuntingMethod huntingMethod) {

        if (!harvestSpecVersion.supportsHarvestPermitState()) {
            throw HarvestSpecVersionNotSupportedException.permitNotSupported(harvestSpecVersion);
        }

        if (harvestPermit.isHarvestReportApproved() || harvestPermit.isHarvestReportRejected()) {
            throw new EndOfHuntingReportExistsException();
        }

        if (harvestPermit.isHarvestReportDone() && mutationRole != HarvestMutationRole.MODERATOR) {
            throw new EndOfHuntingReportExistsException();
        }

        if (!harvestPermit.hasSpeciesAmount(gameSpeciesCode, harvestDate)) {
            throw HarvestPermitSpeciesAmountNotFound.harvestNotValidOn(harvestPermit.getPermitNumber(), gameSpeciesCode, harvestDate);
        }

        final Harvest.StateAcceptedToHarvestPermit stateAcceptedToPermit =
                harvestPermit.getStateAcceptedToPermit(activePerson);

        return new HarvestForPermitMutation(mutationRole, supportsPermittedMethod,
                harvestPermit, stateAcceptedToPermit, permittedMethod, huntingMethod);
    }

    private final HarvestMutationRole mutationRole;
    private final HarvestPermit harvestPermit;
    private final Harvest.StateAcceptedToHarvestPermit stateAcceptedToPermit;
    private final boolean supportsPermittedMethod;
    private final PermittedMethod permittedMethod;
    private final HuntingMethod huntingMethod;

    private HarvestForPermitMutation(@Nonnull final HarvestMutationRole mutationRole,
                                     final boolean supportsPermittedMethod,
                                     @Nonnull final HarvestPermit harvestPermit,
                                     @Nonnull final Harvest.StateAcceptedToHarvestPermit stateAcceptedToPermit,
                                     final PermittedMethod permittedMethod,
                                     final HuntingMethod huntingMethod) {
        this.mutationRole = Objects.requireNonNull(mutationRole);
        this.harvestPermit = Objects.requireNonNull(harvestPermit);
        this.stateAcceptedToPermit = Objects.requireNonNull(stateAcceptedToPermit);
        this.supportsPermittedMethod = supportsPermittedMethod;
        this.permittedMethod = permittedMethod;
        this.huntingMethod = huntingMethod;
    }

    @Override
    public void accept(final Harvest harvest) {
        final boolean roleCanChangePermit = mutationRole == HarvestMutationRole.MODERATOR
                || mutationRole == HarvestMutationRole.AUTHOR_OR_ACTOR;
        final boolean permitChanged = harvest.getHarvestPermit() != null
                && !harvest.getHarvestPermit().equals(harvestPermit);

        if (permitChanged && !roleCanChangePermit) {
            throw new HarvestPermitChangeForbiddenException(mutationRole);
        }

        clearSeasonFields(harvest);

        harvest.setHarvestPermit(harvestPermit);
        harvest.setStateAcceptedToHarvestPermit(stateAcceptedToPermit);
        harvest.setHarvestReportRequired(harvestPermit.isHarvestReportAllowed() &&
                stateAcceptedToPermit == Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        if (!harvestPermit.isPermittedMethodAllowed()) {
            harvest.setPermittedMethod(null);

        } else if (supportsPermittedMethod) {
            harvest.setPermittedMethod(permittedMethod);
        }

        harvest.setHuntingMethod(huntingMethod);
    }

    @Override
    public HarvestReportingType getReportingType() {
        return HarvestReportingType.PERMIT;
    }

    public HarvestPermit getHarvestPermit() {
        return harvestPermit;
    }

    @Override
    public boolean isHarvestReportRequired() {
        // Contact person must accept harvest first
        return stateAcceptedToPermit == Harvest.StateAcceptedToHarvestPermit.ACCEPTED;
    }
}
