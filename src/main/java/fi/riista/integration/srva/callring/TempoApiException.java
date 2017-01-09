package fi.riista.integration.srva.callring;

import com.nsftele.tempo.model.Errors;

import java.util.Objects;

public class TempoApiException extends RuntimeException {
    private final Errors errors;
    private final int status;

    public TempoApiException(final int status, final Errors errors) {
        this.status = status;
        this.errors = Objects.requireNonNull(errors);
    }

    public Errors getErrors() {
        return errors;
    }
}
