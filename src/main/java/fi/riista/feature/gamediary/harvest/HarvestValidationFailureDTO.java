package fi.riista.feature.gamediary.harvest;

import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HarvestValidationFailureDTO {
    public static Error message(@Nonnull final LocalisedString message) {
        Objects.requireNonNull(message, "message is null");
        return new Error(message.asMap());
    }

    public static class Error {
        private Map<String, String> message;

        public Error(final Map<String, String> message) {
            this.message = message;
        }

        public Map<String, String> getMessage() {
            return message;
        }
    }

    private List<Error> errors;

    public HarvestValidationFailureDTO(final List<Error> errors) {
        this.errors = errors;
    }

    public List<Error> getErrors() {
        return errors;
    }
}
