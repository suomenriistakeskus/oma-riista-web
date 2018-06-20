package fi.riista.feature.gamediary.observation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.gamediary.GameDiaryEntitySpecVersion;

import java.util.Optional;

import static fi.riista.feature.gamediary.GameSpecies.isBeaver;

public enum ObservationSpecVersion implements GameDiaryEntitySpecVersion {

    _1,
    _2, // Observation types for beavers changed.
    _3; // Added amount of mooselike calfs and large carnivore specific fields.

    public static final ObservationSpecVersion LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES = _2;
    public static final ObservationSpecVersion LOWEST_VERSION_SUPPORTING_LARGE_CARNIVORE_FIELDS = _3;
    public static final ObservationSpecVersion LOWEST_VERSION_SUPPORTING_CALF_AMOUNT_WITHIN_HUNTING = _3;

    public static final ObservationSpecVersion MOST_RECENT = _3;

    private static final BiMap<ObservationSpecVersion, Integer> INT_ENCODINGS = ImmutableBiMap.of(_1, 1, _2, 2, _3, 3);

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

    public boolean isMostRecent() {
        return this == MOST_RECENT;
    }

    // Mapping between spec-version and metadata-version is currently 1:1.
    public int getMetadataVersion() {
        return toIntValue();
    }

    public boolean supportsExtraBeaverTypes() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES);
    }

    public boolean supportsMooselikeCalfAmount() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_CALF_AMOUNT_WITHIN_HUNTING);
    }

    public boolean supportsLargeCarnivoreFields() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_LARGE_CARNIVORE_FIELDS);
    }

    public boolean requiresBeaverObservationTypeTranslationForMobile(final int gameSpeciesCode) {
        return !supportsExtraBeaverTypes() && isBeaver(gameSpeciesCode);
    }
}
