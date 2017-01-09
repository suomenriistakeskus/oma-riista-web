package fi.riista.feature.account.registration;

import fi.riista.feature.error.MessageExposableValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRegistrationStatus extends MessageExposableValidationException {
    public InvalidRegistrationStatus(final String errorMessage) {
        super(errorMessage);
    }
}
