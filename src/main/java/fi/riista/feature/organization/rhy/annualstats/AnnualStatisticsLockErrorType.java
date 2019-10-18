package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.util.LocalisedEnum;

public enum AnnualStatisticsLockErrorType implements LocalisedEnum {

    COORDINATOR_UPDATES_NOT_ALLOWED_AFTER_SUBMIT,
    END_DATE_FOR_COORDINATOR_UPDATES_PASSED,
    MODERATOR_UPDATES_NOT_ALLOWED_AFTER_APPROVAL
}
