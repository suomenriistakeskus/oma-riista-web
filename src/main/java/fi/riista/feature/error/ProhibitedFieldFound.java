package fi.riista.feature.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProhibitedFieldFound extends MessageExposableValidationException {

    protected ProhibitedFieldFound(final String msg) {
        super(msg);
    }

    public static void assertValueIsNull(@Nullable final Object value,
                                         @Nonnull final String fieldName) {

        if (value != null) {
            throw new ProhibitedFieldFound(getDefaultMessageForProhibitedField(fieldName));
        }
    }

    public static void assertValueIsNull(@Nullable final Object value,
                                         @Nonnull final Supplier<String> errorMessageSupplier) {

        Objects.requireNonNull(errorMessageSupplier, "errorMessageSupplier is null");

        if (value != null) {
            throw new ProhibitedFieldFound(errorMessageSupplier.get());
        }
    }

    public static String getDefaultMessageForProhibitedField(@Nonnull final String fieldName) {
        Objects.requireNonNull(fieldName, "fieldName is null");
        return "Illegal field found: " + fieldName;
    }
}
