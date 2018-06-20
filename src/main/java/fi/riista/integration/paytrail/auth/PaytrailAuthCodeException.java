package fi.riista.integration.paytrail.auth;

public class PaytrailAuthCodeException extends IllegalArgumentException {
    public PaytrailAuthCodeException(final String s) {
        super(s);
    }
}
