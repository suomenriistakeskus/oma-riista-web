package fi.riista.feature.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RequiredFieldMissing extends MessageExposableValidationException {

    protected RequiredFieldMissing(final String msg) {
        super(msg);
    }

    public static void assertValueIsNotNull(@Nullable final Object value, @Nonnull final String fieldName) {
        if (value == null) {
            throw new RequiredFieldMissing(getDefaultMessageForMissingField(fieldName));
        }
    }

    public static void assertValueIsNotNull(@Nullable final Object value,
                                            @Nonnull final Supplier<String> errorMessageSupplier) {

        Objects.requireNonNull(errorMessageSupplier, "errorMessageSupplier is null");

        if (value == null) {
            throw new RequiredFieldMissing(errorMessageSupplier.get());
        }
    }

    public static String getDefaultMessageForMissingField(@Nonnull final String fieldName) {
        Objects.requireNonNull(fieldName, "fieldName is null");
        return "Required field missing: " + fieldName;
    }
}
