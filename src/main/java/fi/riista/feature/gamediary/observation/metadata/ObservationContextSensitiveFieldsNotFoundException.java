package fi.riista.feature.gamediary.observation.metadata;

import fi.riista.feature.error.MessageExposableValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ObservationContextSensitiveFieldsNotFoundException extends MessageExposableValidationException {

    public ObservationContextSensitiveFieldsNotFoundException(final String msg) {
        super(msg);
    }

}
