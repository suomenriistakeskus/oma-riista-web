package fi.riista.feature.common.entity;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public enum RequiredWithinDeerPilot {

    YES, NO, VOLUNTARY, DEER_PILOT;

    public static RequiredWithinDeerPilot from(@Nonnull final Required required) {
        requireNonNull(required);

        switch (required) {
            case YES:
                return RequiredWithinDeerPilot.YES;
            case VOLUNTARY:
                return RequiredWithinDeerPilot.VOLUNTARY;
            case NO:
                return RequiredWithinDeerPilot.NO;
            default:
                // Should never enter here.
                throw new IllegalArgumentException();
        }
    }
}
