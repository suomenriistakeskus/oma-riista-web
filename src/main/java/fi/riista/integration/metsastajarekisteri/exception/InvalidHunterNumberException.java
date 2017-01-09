package fi.riista.integration.metsastajarekisteri.exception;

public class InvalidHunterNumberException extends RuntimeException {
    public InvalidHunterNumberException(String message) {
        super(message);
    }
}
