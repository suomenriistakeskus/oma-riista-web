package fi.riista.integration.metsastajarekisteri.exception;

public class InvalidHunterDateFieldException extends RuntimeException {
    public InvalidHunterDateFieldException(final String message) {
        super(message);
    }
}
