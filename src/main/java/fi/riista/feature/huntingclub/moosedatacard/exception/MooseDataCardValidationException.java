package fi.riista.feature.huntingclub.moosedatacard.exception;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Objects;

public class MooseDataCardValidationException extends MooseDataCardImportException {

    public static MooseDataCardValidationException internalServerError() {
        return of(MooseDataCardImportFailureReasons.internalServerError());
    }

    public static MooseDataCardValidationException parsingXmlFileOfMooseDataCardFailed(
            @Nonnull final String xmlFileName, @Nonnull final Exception parseException) {

        Objects.requireNonNull(parseException, "parseException is null");
        return of(MooseDataCardImportFailureReasons.parsingXmlFileOfMooseDataCardFailed(xmlFileName), parseException);
    }

    public static MooseDataCardValidationException of(@Nonnull final String message) {
        return new MooseDataCardValidationException(message);
    }

    public static MooseDataCardValidationException of(@Nonnull final String message, @Nullable final Throwable cause) {
        return new MooseDataCardValidationException(message, cause);
    }

    public static MooseDataCardValidationException of(@Nonnull final List<String> messages) {
        return new MooseDataCardValidationException(messages);
    }

    public static MooseDataCardValidationException of(
            @Nonnull final List<String> messages, @Nullable final Throwable cause) {

        return new MooseDataCardValidationException(messages, cause);
    }

    public MooseDataCardValidationException(@Nonnull final String message) {
        this(message, null);
    }

    public MooseDataCardValidationException(@Nonnull final String message, @Nullable final Throwable cause) {
        super(message, cause);
    }

    public MooseDataCardValidationException(@Nonnull final List<String> messages) {
        this(messages, null);
    }

    public MooseDataCardValidationException(@Nonnull final List<String> messages, @Nullable final Throwable cause) {
        super(messages, cause);
    }

}
