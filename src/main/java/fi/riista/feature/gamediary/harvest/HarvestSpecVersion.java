package fi.riista.feature.gamediary.harvest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.gamediary.GameDiaryEntitySpecVersion;
import fi.riista.feature.gamediary.GameSpecies;

import java.util.Optional;

public enum HarvestSpecVersion implements GameDiaryEntitySpecVersion {

    _1, // Mobile API v1
    _2, // Mobile API v1 with apiVersion attribute
    _3, // Mobile API v2 initial version
    _4; // Permit based deers can be added extended mooselike specimen fields.

    public static final HarvestSpecVersion LOWEST_VERSION_SUPPORTING_PERMIT_STATE = _2;
    public static final HarvestSpecVersion LOWEST_VERSION_REQUIRING_AMOUNT = _2;
    public static final HarvestSpecVersion LOWEST_VERSION_REQUIRING_LOCATION_SOURCE = _2;
    public static final HarvestSpecVersion LOWEST_VERSION_REQUIRING_SPECIMEN_LIST = _3;
    public static final HarvestSpecVersion LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS = _3;
    public static final HarvestSpecVersion LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS = _4;

    public static final HarvestSpecVersion ONLY_VERSION_USING_DEPRECATED_API_PARAMETER = _2;

    public static final HarvestSpecVersion MOST_RECENT = _4;

    private static final BiMap<HarvestSpecVersion, Integer> INT_ENCODINGS =
            ImmutableBiMap.of(_1, 1, _2, 2, _3, 3, _4, 4);

    @JsonCreator
    public static HarvestSpecVersion fromIntValue(final int value) {
        return Optional
                .ofNullable(INT_ENCODINGS.inverse().get(value))
                .orElseThrow(
                        () -> new MessageExposableValidationException("Unsupported harvestSpecVersion: " + value));
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

    public boolean supportsHarvestPermitState() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_PERMIT_STATE);
    }

    public boolean requiresAmount() {
        return greaterThanOrEqualTo(LOWEST_VERSION_REQUIRING_AMOUNT);
    }

    public boolean requiresGeolocationSource() {
        return greaterThanOrEqualTo(LOWEST_VERSION_REQUIRING_LOCATION_SOURCE);
    }

    public boolean requiresSpecimenList() {
        return greaterThanOrEqualTo(LOWEST_VERSION_REQUIRING_SPECIMEN_LIST);
    }

    public boolean supportsExtendedFieldsForMoose() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS);
    }

    public boolean supportsExtendedFieldsForDeers() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS);
    }

    public boolean requiresDeprecatedApiParameter() {
        return this == ONLY_VERSION_USING_DEPRECATED_API_PARAMETER;
    }

    // Extended moose fields is a super-set of mooselike fields i.e. extended moose fields
    // contain also all mooselike fields.
    public boolean isExtendedMooseFieldsSupported(final int gameSpeciesCode) {
        return GameSpecies.isMoose(gameSpeciesCode) && supportsExtendedFieldsForMoose();
    }

    public boolean isExtendedMooselikeFieldsSupported(final int gameSpeciesCode) {
        return isExtendedMooseFieldsSupported(gameSpeciesCode) ||
                GameSpecies.isDeerRequiringPermitForHunting(gameSpeciesCode) && supportsExtendedFieldsForDeers();
    }

}
