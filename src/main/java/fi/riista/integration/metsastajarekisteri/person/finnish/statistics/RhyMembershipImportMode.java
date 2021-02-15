package fi.riista.integration.metsastajarekisteri.person.finnish.statistics;

import org.joda.time.DateTime;

public class RhyMembershipImportMode {

    // Safety margin is used for storing the membership status at the end of the year
    // and storing payment info after year change. Since the import is between two systems,
    // Use safety margin in case import fails or is not triggered at all at specific date.
    /*package*/ static final int SYNC_SAFETY_MARGIN_MEMBERSHIP_DAYS = 5;
    /*package*/ static final int SYNC_SAFETY_MARGIN_PAYMENT_DAYS = 15;

    public enum Phase {
        STORE_MEMBERS,
        STORE_PAYMENT,
        NOT_APPLICABLE
    }

    public static Phase getPhase(final DateTime date) {
        if (isEndOfYearInsideSafetyMargin(date)) {
            return Phase.STORE_MEMBERS;
        } else if (isBeginOfYearInsideSafetyMargin(date)) {
            return Phase.STORE_PAYMENT;
        } else {
            return Phase.NOT_APPLICABLE;
        }
    }

    private static boolean isBeginOfYearInsideSafetyMargin(final DateTime syncTime) {
        final int syncYear = syncTime.getYear();
        return syncYear > syncTime.minusDays(SYNC_SAFETY_MARGIN_PAYMENT_DAYS).getYear();
    }

    private static boolean isEndOfYearInsideSafetyMargin(final DateTime syncTime) {
        final int syncYear = syncTime.getYear();
        return syncYear < syncTime.plusDays(SYNC_SAFETY_MARGIN_MEMBERSHIP_DAYS).getYear();
    }
}
