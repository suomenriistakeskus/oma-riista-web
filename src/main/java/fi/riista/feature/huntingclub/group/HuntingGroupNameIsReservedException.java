package fi.riista.feature.huntingclub.group;

import javax.annotation.Nonnull;

import java.util.Objects;

public class HuntingGroupNameIsReservedException extends RuntimeException {

    private final String conflictingGroupName;

    public HuntingGroupNameIsReservedException(@Nonnull final String conflictingGroupName) {
        this.conflictingGroupName = Objects.requireNonNull(conflictingGroupName);
    }

    public String getConflictingGroupName() {
        return conflictingGroupName;
    }

}
