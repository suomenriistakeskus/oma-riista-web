package fi.riista.integration.metsastajarekisteri.exception;

public class IllegalAgeException extends RuntimeException {
    public IllegalAgeException(final String message) {
        super(message);
    }
}
