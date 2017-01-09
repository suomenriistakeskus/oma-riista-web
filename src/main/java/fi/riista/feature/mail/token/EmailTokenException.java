package fi.riista.feature.mail.token;

import fi.riista.feature.error.MessageExposableValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmailTokenException extends MessageExposableValidationException {
    public EmailTokenException(final String errorMessage) {
        super(errorMessage);
    }
}
