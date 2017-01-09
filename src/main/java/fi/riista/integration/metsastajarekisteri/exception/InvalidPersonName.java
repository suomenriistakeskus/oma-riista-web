package fi.riista.integration.metsastajarekisteri.exception;

public class InvalidPersonName extends RuntimeException {
    public InvalidPersonName(String message) {
        super(message);
    }
}
