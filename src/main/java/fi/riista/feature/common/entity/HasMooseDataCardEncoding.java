package fi.riista.feature.common.entity;

import fi.riista.util.F;
import io.vavr.collection.Stream;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

public interface HasMooseDataCardEncoding<E extends Enum<E>> {

    @Nonnull
    static <E extends Enum<E> & HasMooseDataCardEncoding<E>> Either<Optional<String>, E> eitherInvalidOrValid(
            @Nonnull final Class<E> enumClass, @Nullable final String value) {

        final String trimmed = StringUtils.trimToNull(value);

        return trimmed == null
                ? Either.left(Optional.empty())
                : Stream.of(enumClass.getEnumConstants())
                        .find(val -> Option.of(val.getMooseDataCardEncoding()).exists(trimmed::equalsIgnoreCase))
                        .toRight(() -> Optional.of(trimmed));
    }

    @Nonnull
    static <E extends Enum<E> & HasMooseDataCardEncoding<E>> Optional<E> findEnum(@Nonnull final Class<E> enumClass,
                                                                                  @Nullable final String value) {
        return F.toOptional(eitherInvalidOrValid(enumClass, value));
    }

    @Nullable
    static <E extends Enum<E> & HasMooseDataCardEncoding<E>> E getEnumOrNull(@Nonnull final Class<E> enumClass,
                                                                             @Nullable final String value) {

        return eitherInvalidOrValid(enumClass, value).getOrElseGet(invalid -> null);
    }

    @Nonnull
    static <E extends Enum<E> & HasMooseDataCardEncoding<E>, X extends RuntimeException> E getEnumOrThrow(
            @Nonnull final Class<E> enumClass,
            @Nullable final String value,
            @Nonnull final Function<Optional<String>, X> exceptionFn) {

        return eitherInvalidOrValid(enumClass, value).getOrElseThrow(exceptionFn);
    }

    @Nonnull
    static <E extends Enum<E> & HasMooseDataCardEncoding<E>> E getEnum(@Nonnull final Class<E> enumClass,
                                                                       @Nullable final String value) {

        return getEnumOrThrow(enumClass, value, invalidOpt -> {
            final String invalid = invalidOpt.map(s -> '"' + s + '"').orElse("null");
            return new IllegalArgumentException(
                    String.format("Could not convert to %s from %s", enumClass.getSimpleName(), invalid));
        });
    }

    @Nullable
    String getMooseDataCardEncoding();

    default boolean equalsMooseDataCardEncoding(@Nullable final String value) {
        final String trimmed = StringUtils.trimToNull(value);
        return trimmed != null && Option.of(getMooseDataCardEncoding()).exists(trimmed::equalsIgnoreCase);
    }
}
