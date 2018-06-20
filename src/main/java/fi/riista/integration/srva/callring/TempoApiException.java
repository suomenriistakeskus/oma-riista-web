package fi.riista.integration.srva.callring;

import com.google.common.base.MoreObjects;
import com.nsftele.tempo.model.Error;
import com.nsftele.tempo.model.Errors;

import java.util.Objects;

public class TempoApiException extends RuntimeException {
    private static String getMessage(final int status, final Errors errors) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Error status: ");
        sb.append(status);
        sb.append(" errors:");

        for (Error error : errors.getErrors()) {
            sb.append("\n");
            sb.append(MoreObjects.toStringHelper(error)
                    .add("key", error.getKey())
                    .add("value", error.getValue())
                    .add("property", error.getProperty())
                    .add("message", error.getMsg())
                    .toString());
        }

        return sb.toString();
    }

    private final Errors errors;
    private final int status;

    public TempoApiException(final int status, final Errors errors) {
        super(getMessage(status, errors));
        this.status = status;
        this.errors = Objects.requireNonNull(errors);
    }

    public Errors getErrors() {
        return errors;
    }
}
