package fi.riista.feature.gamediary.harvest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.gamediary.GameDiaryEntitySpecVersion;

import java.util.Optional;

public enum HarvestSpecVersion implements GameDiaryEntitySpecVersion {

    // Historical versions:
    // _1, - Mobile API v1
    // _2, - Mobile API v1 with apiVersion attribute

    _3, // 2015-11: Mobile API v2 initial version
    _4, // 2016-10: Permit based deers can be added extended specimen fields.
    _5, // 2017-05: Added lonesome boolean flag for moose calf specimens.
    _6, // 2017-11: Support for harvest report
    _7, // 2020-05: Deer hunting type
    _8; // 2020-12: Add new (more) antler fields for moose and deers and weight fields for roe deer and wild boar.

    public static final HarvestSpecVersion LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS = _4;
    public static final HarvestSpecVersion LOWEST_VERSION_SUPPORTING_SOLITARY_CALVES = _5;
    public static final HarvestSpecVersion LOWEST_VERSION_SUPPORTING_HARVEST_REPORT = _6;
    public static final HarvestSpecVersion LOWEST_VERSION_SUPPORTING_DEER_HUNTING_TYPE = _7;
    public static final HarvestSpecVersion LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020 = _8;
    public static final HarvestSpecVersion LOWEST_VERSION_SUPPORTING_EXTENDED_WEIGHT_FIELDS_FOR_ROE_DEER_WILD_BOAR = _8;

    public static final HarvestSpecVersion MOST_RECENT = _8;
    public static final HarvestSpecVersion CURRENTLY_SUPPORTED = MOST_RECENT;

    private static final BiMap<HarvestSpecVersion, Integer> INT_ENCODINGS =
            new ImmutableBiMap.Builder<HarvestSpecVersion, Integer>()
                    .put(_3, 3)
                    .put(_4, 4)
                    .put(_5, 5)
                    .put(_6, 6)
                    .put(_7, 7)
                    .put(_8, 8)
                    .build();

    @JsonCreator
    public static HarvestSpecVersion fromIntValue(final int value) {
        return Optional
                .ofNullable(INT_ENCODINGS.inverse().get(value))
                .orElseThrow(() -> new MessageExposableValidationException("Unsupported harvestSpecVersion: " + value));
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

    public boolean supportsHarvestReport() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_HARVEST_REPORT);
    }

    public boolean supportsExtendedFieldsForDeers() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS);
    }

    public boolean supportsSolitaryMooseCalves() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_SOLITARY_CALVES);
    }

    public boolean supportsDeerHuntingType() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_DEER_HUNTING_TYPE);
    }

    public boolean supportsAntlerFields2020() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020);
    }

    public boolean supportsExtendedWeightFieldsForRoeDeerAndWildBoar() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_EXTENDED_WEIGHT_FIELDS_FOR_ROE_DEER_WILD_BOAR);
    }
}
