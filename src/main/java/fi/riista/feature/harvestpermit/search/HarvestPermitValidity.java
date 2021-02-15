package fi.riista.feature.harvestpermit.search;

import fi.riista.util.LocalisedEnum;

public enum HarvestPermitValidity implements LocalisedEnum {
    // begin date in the past and end date in the future
    ACTIVE,
    // both begin and end date in the past
    PASSED,
    // both begin and end date in the future
    FUTURE,
    // permit has no species amounts
    UNKNOWN
}
