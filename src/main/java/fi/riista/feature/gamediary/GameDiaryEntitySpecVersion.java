package fi.riista.feature.gamediary;

import javax.annotation.Nonnull;

import java.util.Objects;

public interface GameDiaryEntitySpecVersion {

    int toIntValue();

    default int compareTo(@Nonnull final GameDiaryEntitySpecVersion other) {
        Objects.requireNonNull(other);
        return toIntValue() - other.toIntValue();
    }

    default boolean lessThan(@Nonnull final GameDiaryEntitySpecVersion other) {
        return compareTo(other) < 0;
    }

    default boolean greaterThanOrEqualTo(@Nonnull final GameDiaryEntitySpecVersion other) {
        return compareTo(other) >= 0;
    }

}
