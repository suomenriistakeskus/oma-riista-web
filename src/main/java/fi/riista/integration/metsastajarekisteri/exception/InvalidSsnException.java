package fi.riista.integration.metsastajarekisteri.exception;

public class InvalidSsnException extends RuntimeException {
    public InvalidSsnException(String message) {
        super(message);
    }
}
