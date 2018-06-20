package fi.riista.integration.fivaldi;

public class FivaldiRecordParseException extends RuntimeException {

    public FivaldiRecordParseException(final String message) {
        super(message);
    }

    public FivaldiRecordParseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
