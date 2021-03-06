package fi.riista.feature.shootingtest.registration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CannotRegisterShootingTestParticipantException extends RuntimeException {

    public CannotRegisterShootingTestParticipantException(final String msg) {
        super(msg);
    }
}
