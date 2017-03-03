package fi.riista.feature.account.registration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRegistrationStatus extends VetumaTransactionException {
    public InvalidRegistrationStatus(final String errorMessage) {
        super(errorMessage);
    }
}
