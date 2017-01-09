package fi.riista.feature.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception mapped to not implemented Error HTTP status code (501)
 */
@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_IMPLEMENTED)
public class NotImplementedException extends RuntimeException {
    public NotImplementedException() {
    }

    public NotImplementedException(String message) {
        super(message);
    }

    public NotImplementedException(Throwable throwable) {
        super(throwable);
    }

    public NotImplementedException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
