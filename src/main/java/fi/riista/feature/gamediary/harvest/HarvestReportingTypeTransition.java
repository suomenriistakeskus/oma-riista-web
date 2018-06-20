package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.gamediary.harvest.mutation.HarvestMutationRole;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestReportingTypeChangeForbiddenException;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class HarvestReportingTypeTransition {
    private final HarvestReportingType from;
    private final HarvestReportingType to;
    private final HarvestMutationRole mutationRole;

    public HarvestReportingTypeTransition(@Nonnull final HarvestReportingType from,
                                          @Nonnull final HarvestReportingType to,
                                          @Nonnull final HarvestMutationRole mutationRole) {
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
        this.mutationRole = Objects.requireNonNull(mutationRole);
    }

    public void assertValidTransition() {
        if (!isStateTransitionValid()) {
            throw new HarvestReportingTypeChangeForbiddenException(from, to);
        }
    }

    private boolean isStateTransitionValid() {
        if (!isReportingTypeChanged()) {
            return true;
        }

        if (isDetachToHuntingDay()) {
            return false;
        }

        switch (mutationRole) {
            case AUTHOR_OR_ACTOR:
                return true;
            case PERMIT_CONTACT_PERSON:
                return false;
            case MODERATOR:
                return from == HarvestReportingType.BASIC;
            case OTHER:
                return isAttachToHuntingDay();
            default:
                return false;
        }
    }

    private boolean isReportingTypeChanged() {
        return from != to;
    }

    private boolean isAttachToHuntingDay() {
        return from == HarvestReportingType.BASIC &&
                to == HarvestReportingType.HUNTING_DAY;
    }

    private boolean isDetachToHuntingDay() {
        return from == HarvestReportingType.HUNTING_DAY &&
                to != HarvestReportingType.HUNTING_DAY;
    }
}
