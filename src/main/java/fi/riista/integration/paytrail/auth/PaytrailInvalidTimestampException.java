package fi.riista.integration.paytrail.auth;

public class PaytrailInvalidTimestampException extends IllegalStateException {
    public PaytrailInvalidTimestampException(final String s) {
        super(s);
    }
}
