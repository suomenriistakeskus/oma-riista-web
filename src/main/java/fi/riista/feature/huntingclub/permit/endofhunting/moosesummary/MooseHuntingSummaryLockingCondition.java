package fi.riista.feature.huntingclub.permit.endofhunting.moosesummary;

import fi.riista.util.F;

import java.util.function.BooleanSupplier;

import static java.util.Objects.requireNonNull;

public class MooseHuntingSummaryLockingCondition {

    private BooleanSupplier moderator;
    private BooleanSupplier mooseDataCard;
    private BooleanSupplier permitHolderFinishedHunting;
    private BooleanSupplier permitLockDatePassed;
    private BooleanSupplier permittedByOccupation;

    public static Builder builder() {
        return new Builder();
    }

    private MooseHuntingSummaryLockingCondition() {
    }

    private void assertAllPropertiesSet() {
        if (F.anyNull(moderator, mooseDataCard, permitHolderFinishedHunting, permitLockDatePassed, permittedByOccupation)) {
            throw new IllegalStateException(String.format(
                    "Some property of %s instance is null", this.getClass().getSimpleName()));
        }
    }

    public boolean isLocked() {
        return permitHolderFinishedHunting.getAsBoolean()
                || !moderator.getAsBoolean() && (mooseDataCard.getAsBoolean() || permitLockDatePassed.getAsBoolean())
                || !permittedByOccupation.getAsBoolean();
    }

    public static class Builder {

        private MooseHuntingSummaryLockingCondition state = new MooseHuntingSummaryLockingCondition();

        public MooseHuntingSummaryLockingCondition build() {
            state.assertAllPropertiesSet();
            return state;
        }

        public Builder withActionRequestedByModerator(final BooleanSupplier actionRequestedByModerator) {
            state.moderator = requireNonNull(actionRequestedByModerator);
            return this;
        }

        public Builder withDataOriginatingFromMooseDataCard(final BooleanSupplier dataOriginatingFromMooseDataCard) {
            state.mooseDataCard = requireNonNull(dataOriginatingFromMooseDataCard);
            return this;
        }

        public Builder withPermitHolderFinishedHunting(final BooleanSupplier permitHolderFinishedHunting) {
            state.permitHolderFinishedHunting = requireNonNull(permitHolderFinishedHunting);
            return this;
        }

        public Builder withPermitLockDatePassed(final BooleanSupplier permitLockDatePassed) {
            state.permitLockDatePassed = requireNonNull(permitLockDatePassed);
            return this;
        }

        public Builder withUserHavingOccupationGrantingPermission(final BooleanSupplier permittedByOccupation) {
            state.permittedByOccupation = requireNonNull(permittedByOccupation);
            return this;
        }
    }
}
