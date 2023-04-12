package fi.riista.feature.organization.rhy.huntingcontrolevent.mobile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import fi.riista.feature.error.MessageExposableValidationException;

import java.util.Optional;

public enum MobileHuntingControlSpecVersion {

    _1;

    public static final MobileHuntingControlSpecVersion MOST_RECENT = _1;

    private static final BiMap<MobileHuntingControlSpecVersion, Integer> INT_ENCODINGS = ImmutableBiMap.of(_1, 1);

    @JsonCreator
    public static MobileHuntingControlSpecVersion fromIntValue(final int value) {
        return Optional.ofNullable(INT_ENCODINGS.inverse().get(value))
                .orElseThrow(() -> new MessageExposableValidationException("Unsupported MobileHuntingControlSpecVersion: " + value));
    }

    @JsonValue
    public int toIntValue() {
        return INT_ENCODINGS.get(this);
    }

    @Override
    public String toString() {
        return String.valueOf(toIntValue());
    }
}
