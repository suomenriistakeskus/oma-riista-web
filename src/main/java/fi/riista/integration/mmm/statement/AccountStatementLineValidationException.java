package fi.riista.integration.mmm.statement;

public class AccountStatementLineValidationException extends AccountStatementParseException {

    public AccountStatementLineValidationException(final String message) {
        super(message);
    }

    public AccountStatementLineValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
