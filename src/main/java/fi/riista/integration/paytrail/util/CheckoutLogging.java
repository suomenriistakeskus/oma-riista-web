package fi.riista.integration.paytrail.util;

import org.slf4j.Logger;

public class CheckoutLogging {
    private static final String PAYTRAIL_ERROR_LOGGING_PREFIX = "Paytrail payment handling failed";
    public static final void logFailure(final Logger logger, final String message) {
        logger.warn("{}: {}", PAYTRAIL_ERROR_LOGGING_PREFIX, message);
    }
}
