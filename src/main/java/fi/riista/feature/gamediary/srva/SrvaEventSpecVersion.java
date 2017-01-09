package fi.riista.feature.gamediary.srva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.gamediary.GameDiaryEntitySpecVersion;

import java.util.Optional;

public enum SrvaEventSpecVersion implements GameDiaryEntitySpecVersion {

    _1; // Mobile API v2

    public static final SrvaEventSpecVersion MOST_RECENT = _1;

    private static final BiMap<SrvaEventSpecVersion, Integer> INT_ENCODINGS = ImmutableBiMap.of(_1, 1);

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

}
