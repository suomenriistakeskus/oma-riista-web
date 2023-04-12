package fi.riista.integration.paytrail.auth;

public class InvalidSignatureException extends RuntimeException {
    public InvalidSignatureException() {
        super("No valid signature found from response.");
    }
}
