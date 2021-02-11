package fi.riista.feature.gamediary.observation;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class ObservationLockInfo {

    private final ObservationModifierInfo modifierInfo;

    // When observation is locked only description and images can be updated by the author/observer.
    private final boolean locked;

    private final boolean canChangeHuntingDay;

    public ObservationLockInfo(@Nonnull final ObservationModifierInfo modifierInfo,
                               final boolean locked,
                               final boolean canChangeHuntingDay) {

        this.modifierInfo = requireNonNull(modifierInfo);

        this.locked = locked;
        this.canChangeHuntingDay = canChangeHuntingDay;
    }

    // Accessors -->

    public ObservationModifierInfo getModifierInfo() {
        return modifierInfo;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean canChangeHuntingDay() {
        return canChangeHuntingDay;
    }
}
