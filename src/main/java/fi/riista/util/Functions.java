package fi.riista.util;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.common.entity.HasID;
import javaslang.Function2;
import javaslang.Tuple;
import javaslang.Tuple2;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class Functions {

    public static final Function<ConstraintViolation<?>, String> CONSTRAINT_VIOLATION_TO_STRING = violation -> {
        if (violation == null) {
            return null;
        }

        // Values enclosed in curly brackets are stripped away (e.g. by @Range).
        final String filteredMessage = Optional.ofNullable(violation.getMessage())
                .filter(msg -> !msg.matches("^\\{.+\\}$"))
                .map(msg -> msg.replaceAll("\\{.+\\} ?", ""))
                .map(msg -> String.format(" %s, was", msg))
                .orElse("");

        return String.format(
                "%s.%s%s: %s [@%s]",
                violation.getRootBeanClass().getSimpleName(),
                violation.getPropertyPath(),
                filteredMessage,
                StringUtils.quoteIfString(violation.getInvalidValue()),
                violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName());
    };

    private Functions() {
        throw new AssertionError();
    }

    @Nonnull
    public static <T, ID, U extends HasID<ID>> Function<T, ID> idOf(@Nonnull final Function<? super T, U> function) {
        Objects.requireNonNull(function);
        return t -> F.getId(function.apply(t));
    }

    @Nonnull
    public static <K, V> Function<K, V> forMap(@Nonnull final Map<K, ? extends V> map) {
        Objects.requireNonNull(map);

        return key -> map.computeIfAbsent(key, k -> {
            throw new IllegalArgumentException("No value associated with key '" + k + "' in map");
        });
    }

    @Nonnull
    public static <K, V> Function<K, V> forMap(@Nonnull final Map<K, V> map, @Nullable final V defaultValue) {
        Objects.requireNonNull(map, "map is null");
        return key -> map.getOrDefault(key, defaultValue);
    }

    @Nonnull
    public static <T, U> Function2<T, U, T> firstOf2() {
        return (first, second) -> first;
    }

    @Nonnull
    public static <ID extends Serializable> Function<BaseEntity<ID>, Tuple2<ID, Integer>> idAndVersion() {
        return entity -> Optional.ofNullable(entity)
                .map(e -> Tuple.of(e.getId(), e.getConsistencyVersion()))
                .orElse(null);
    }

    @Nonnull
    public static <ID extends Serializable> Function<BaseEntityDTO<ID>, Tuple2<ID, Integer>> dtoIdAndVersion() {
        return dto -> Optional.ofNullable(dto)
                .map(d -> Tuple.of(dto.getId(), dto.getRev()))
                .orElse(null);
    }

    @Nonnull
    public static <T, U> BiFunction<T, U, Boolean> withNotNullChecks(@Nonnull final BiFunction<T, U, Boolean> function) {
        Objects.requireNonNull(function);
        return (first, second) -> first != null && second != null && function.apply(first, second);
    }

    @Nonnull
    public static <T extends HasID<ID>, U extends HasID<ID>, ID> BiFunction<T, U, Boolean> withIdCompare(
            @Nonnull final BiFunction<T, U, Boolean> function) {

        Objects.requireNonNull(function);
        return (first, second) -> Objects.equals(F.getId(first), F.getId(second)) && function.apply(first, second);
    }

}
