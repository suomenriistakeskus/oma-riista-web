package fi.riista.integration.mmm.statement;

import static java.lang.String.format;

public class AccountStatementParseException extends RuntimeException {

    public static AccountStatementParseException invalidContent(final int lineNum, final String content) {
        return new AccountStatementParseException(format("Invalid account statement line at %d: %s", lineNum, content));
    }

    public AccountStatementParseException(final String message) {
        super(message);
    }

    public AccountStatementParseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
