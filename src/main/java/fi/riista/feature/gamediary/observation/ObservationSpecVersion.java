package fi.riista.feature.gamediary.observation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.gamediary.GameDiaryEntitySpecVersion;
import fi.riista.feature.gamediary.GameSpecies;

import java.util.Optional;

public enum ObservationSpecVersion implements GameDiaryEntitySpecVersion {

    _1, _2;

    public static final ObservationSpecVersion LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES = _2;

    public static final ObservationSpecVersion MOST_RECENT = _2;

    private static final BiMap<ObservationSpecVersion, Integer> INT_ENCODINGS = ImmutableBiMap.of(_1, 1, _2, 2);

    @JsonCreator
    public static ObservationSpecVersion fromIntValue(final int value) {
        return Optional
                .ofNullable(INT_ENCODINGS.inverse().get(value))
                .orElseThrow(
                        () -> new MessageExposableValidationException("Unsupported observationSpecVersion: " + value));
    }

    @JsonValue
    @Override
    public int toIntValue() {
        return INT_ENCODINGS.get(this);
    }

    @Override
    public String toString() {
        return String.valueOf(toIntValue());
    }

    public boolean supportsExtraBeaverTypes() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES);
    }

    public boolean requiresBeaverObservationTypeTranslationForMobile(final int gameSpeciesCode) {
        return !supportsExtraBeaverTypes() && GameSpecies.isBeaver(gameSpeciesCode);
    }

}
