package fi.riista.feature.gamediary.srva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.gamediary.GameDiaryEntitySpecVersion;
import fi.riista.feature.gamediary.srva.method.SrvaMethodEnum;

import java.util.Optional;

public enum SrvaEventSpecVersion implements GameDiaryEntitySpecVersion {

    _1, // Mobile API v2
    _2; // Additional fields for SRVA event

    public static final SrvaEventSpecVersion MOST_RECENT = _2;
    public static final SrvaEventSpecVersion LOWEST_VERSION_SUPPORTING_XTRA_SRVA_METHODS = _2;
    public static final SrvaEventSpecVersion LOWEST_VERSION_SUPPORTING_SRVA_DETAILS = _2;


    private static final BiMap<SrvaEventSpecVersion, Integer> INT_ENCODINGS = ImmutableBiMap.of(
            _1, 1,
            _2, 2
    );

    @JsonCreator
    public static SrvaEventSpecVersion fromIntValue(final int value) {
        return Optional
                .ofNullable(INT_ENCODINGS.inverse().get(value))
                .orElseThrow(
                        () -> new MessageExposableValidationException("Unsupported SrvaEventSpecVersion: " + value));
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

    public boolean supportsMethod(final SrvaMethodEnum method) {
        switch (method) {
            case VEHICLE:
            case CHASING_WITH_PEOPLE:
                return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_XTRA_SRVA_METHODS);
            default:
                return true;
        }
    }

    public boolean supportsSrvaDetails() {
        return this.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_SRVA_DETAILS);
    }
}
