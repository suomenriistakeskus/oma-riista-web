package fi.riista.feature.gamediary.harvest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.gamediary.GameDiaryEntitySpecVersion;

import javax.annotation.Nullable;
import java.util.Optional;

import static fi.riista.feature.gamediary.GameSpecies.isDeerRequiringPermitForHunting;
import static fi.riista.feature.gamediary.GameSpecies.isMoose;

public enum HarvestSpecVersion implements GameDiaryEntitySpecVersion {

    _1, // Mobile API v1
    _2, // Mobile API v1 with apiVersion attribute
    _3, // Mobile API v2 initial version
    _4, // Permit based deers can be added extended mooselike specimen fields.
    _5, // Added lonesome boolean flag for moose calf specimens.
    _6; // Support for harvest report

    public static final HarvestSpecVersion LOWEST_VERSION_SUPPORTING_PERMIT_STATE = _2;
    public static final HarvestSpecVersion LOWEST_VERSION_REQUIRING_AMOUNT = _2;
    public static final HarvestSpecVersion LOWEST_VERSION_REQUIRING_LOCATION_SOURCE = _2;
    public static final HarvestSpecVersion LOWEST_VERSION_REQUIRING_SPECIMEN_LIST = _3;
    public static final HarvestSpecVersion LOWEST_VERSION_SUPPORTING_EXTENDED_MOOSE_FIELDS = _3;
    public static final HarvestSpecVersion LOWEST_VERSION_SUPPORTING_EXTENDED_DEER_FIELDS = _4;
    public static final HarvestSpecVersion LOWEST_VERSION_SUPPORTING_SOLITARY_CALVES = _5;
    public static final HarvestSpecVersion LOWEST_VERSION_SUPPORTING_HARVEST_REPORT = _6;

    public static final HarvestSpecVersion ONLY_VERSION_USING_DEPRECATED_API_PARAMETER = _2;

    public static final HarvestSpecVersion MOST_RECENT = _6;

    private static final BiMap<HarvestSpecVersion, Integer> INT_ENCODINGS = new ImmutableBiMap.Builder<HarvestSpecVersion, Integer>()
            .put(_1, 1)
            .put(_2, 2)
            .put(_3, 3)
            .put(_4, 4)
            .put(_5, 5)
            .put(_6, 6)
            .build();

    @JsonCreator
    public static HarvestSpecVersion fromIntValue(final int value) {
        return Optional.ofNullable(INT_ENCODINGS.inverse().get(value))
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

    public void assertSupports(@Nullable final HarvestReportingType reportingType, final boolean mobileClient) {
        if (reportingType == HarvestReportingType.HUNTING_DAY && mobileClient) {
            throw HarvestSpecVersionNotSupportedException.groupHuntingNotSupported(this);
        }

        if (reportingType == HarvestReportingType.PERMIT && !supportsHarvestPermitState()) {
            throw HarvestSpecVersionNotSupportedException.permitNotSupported(this);
        }

        if (reportingType == HarvestReportingType.SEASON && !supportsHarvestReport()) {
            throw HarvestSpecVersionNotSupportedException.seasonNotSupported(this);
        }
    }

    public boolean supportsHarvestPermitState() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_PERMIT_STATE);
    }

    public boolean supportsHarvestReport() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_HARVEST_REPORT);
    }

    public boolean supportGroupHuntingDay() {
        return false;
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

    public boolean supportsSolitaryMooseCalves() {
        return greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_SOLITARY_CALVES);
    }

    public boolean requiresDeprecatedApiParameter() {
        return this == ONLY_VERSION_USING_DEPRECATED_API_PARAMETER;
    }

    // Extended moose fields is a super-set of mooselike fields i.e. extended moose fields
    // contain also all mooselike fields.
    public boolean isPresenceOfMooseFieldsLegitimate(final int gameSpeciesCode) {
        return isMoose(gameSpeciesCode) && supportsExtendedFieldsForMoose();
    }

    public boolean isPresenceOfMooselikeFieldsLegitimate(final int gameSpeciesCode) {
        return isPresenceOfMooseFieldsLegitimate(gameSpeciesCode) ||
                isDeerRequiringPermitForHunting(gameSpeciesCode) && supportsExtendedFieldsForDeers();
    }

    public boolean isPresenceOfAloneLegitimate(final int gameSpeciesCode) {
        return isMoose(gameSpeciesCode) && supportsSolitaryMooseCalves();
    }
}
