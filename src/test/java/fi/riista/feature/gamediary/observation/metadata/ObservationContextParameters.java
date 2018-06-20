package fi.riista.feature.gamediary.observation.metadata;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Contains attributes required in population of observation fields that cannot
 * be determined statically (e.g. user role dependent data).
 */
public class ObservationContextParameters {

    private final BooleanSupplier userAssignedCarnivoreAuthority;

    public ObservationContextParameters(@Nonnull final BooleanSupplier userAssignedCarnivoreAuthority) {
        this.userAssignedCarnivoreAuthority = Objects.requireNonNull(userAssignedCarnivoreAuthority);
    }

    public boolean isUserAssignedCarnivoreAuthority() {
        return userAssignedCarnivoreAuthority.getAsBoolean();
    }
}
