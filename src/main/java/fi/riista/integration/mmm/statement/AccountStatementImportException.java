package fi.riista.integration.mmm.statement;

public class AccountStatementImportException extends IllegalStateException {

    public AccountStatementImportException(final String message) {
        super(message);
    }

    public AccountStatementImportException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
