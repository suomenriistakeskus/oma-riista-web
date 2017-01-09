package fi.riista.integration.metsastajarekisteri.exception;

public class InvalidHunterInvoiceReferenceException extends RuntimeException {
    public InvalidHunterInvoiceReferenceException(final String message) {
        super(message);
    }
}
