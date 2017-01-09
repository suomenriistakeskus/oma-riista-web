package fi.riista.feature.error;

import javax.validation.ValidationException;

public class MessageExposableValidationException extends ValidationException {
    public MessageExposableValidationException(final String errorMessage) {
        super(errorMessage);
    }
}
