package fi.riista.feature.organization.rhy.annualstats;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public interface HasLastModificationStatus<T> {

    boolean isEqualTo(T other);

    void updateModificationStatus();

    default void updateModificationStatusIfNotEqualTo(@Nonnull final T other) {
        requireNonNull(other);

        if (!isEqualTo(other)) {
            updateModificationStatus();
        }
    }
}
