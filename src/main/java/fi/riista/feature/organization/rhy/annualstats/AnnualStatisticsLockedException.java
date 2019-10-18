package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.error.MessageExposableValidationException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsLockErrorType.COORDINATOR_UPDATES_NOT_ALLOWED_AFTER_SUBMIT;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsLockErrorType.END_DATE_FOR_COORDINATOR_UPDATES_PASSED;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsLockErrorType.MODERATOR_UPDATES_NOT_ALLOWED_AFTER_APPROVAL;
import static java.util.Objects.requireNonNull;

public class AnnualStatisticsLockedException extends MessageExposableValidationException {

    public static AnnualStatisticsLockedException coordinatorUpdatesNotAllowedAfterSubmit(final EnumLocaliser localiser) {
        return from(COORDINATOR_UPDATES_NOT_ALLOWED_AFTER_SUBMIT, localiser);
    }

    public static AnnualStatisticsLockedException endDateForCoordinatorUpdatesPassed(final EnumLocaliser localiser) {
        return from(END_DATE_FOR_COORDINATOR_UPDATES_PASSED, localiser);
    }

    public static AnnualStatisticsLockedException moderatorUpdatesNotAllowedAfterApproval(final EnumLocaliser localiser) {
        return from(MODERATOR_UPDATES_NOT_ALLOWED_AFTER_APPROVAL, localiser);
    }

    static AnnualStatisticsLockedException from(@Nonnull final AnnualStatisticsLockErrorType lockErrType,
                                                @Nullable final EnumLocaliser localiser) {

        requireNonNull(lockErrType);

        // localiser may be null in unit tests.
        final String message = localiser != null ? localiser.getTranslation(lockErrType) : lockErrType.name();

        return new AnnualStatisticsLockedException(message);
    }

    public AnnualStatisticsLockedException(final String message) {
        super(message);
    }
}
