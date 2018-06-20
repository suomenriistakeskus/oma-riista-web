package fi.riista.feature.shootingtest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalShootingTestParticipantStateException extends RuntimeException {

    public IllegalShootingTestParticipantStateException(final String msg) {
        super(msg);
    }
}
