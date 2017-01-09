package fi.riista.feature.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception mapped to Conflict HTTP status code (409)
 */
@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.CONFLICT)
public class RevisionConflictException extends RuntimeException {

    public RevisionConflictException() {
        super();
    }

    public RevisionConflictException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RevisionConflictException(final String message) {
        super(message);
    }

    public RevisionConflictException(final Throwable cause) {
        super(cause);
    }
}
