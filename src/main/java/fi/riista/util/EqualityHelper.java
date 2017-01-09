package fi.riista.util;

import fi.riista.feature.common.entity.HasID;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;

import static fi.riista.util.Functions.withIdCompare;
import static fi.riista.util.Functions.withNotNullChecks;

public final class EqualityHelper {

    private EqualityHelper() {
        throw new AssertionError();
    }

    public static <T, U> boolean equal(
            @Nonnull final Iterable<T> i1,
            @Nonnull final Iterable<U> i2,
            @Nonnull final BiFunction<T, U, Boolean> compareFn) {

        Objects.requireNonNull(i1, "i1 is null");
        Objects.requireNonNull(i2, "i2 is null");
        Objects.requireNonNull(compareFn, "compareFn is null");

        final Iterator<T> firstIter = i1.iterator();
        final Iterator<U> secondIter = i2.iterator();

        while (firstIter.hasNext()) {
            if (!secondIter.hasNext() || !compareFn.apply(firstIter.next(), secondIter.next())) {
                return false;
            }
        }

        return !secondIter.hasNext();
    }

    public static <T, U> boolean equalNotNull(
            @Nonnull final Iterable<T> i1,
            @Nonnull final Iterable<U> i2,
            @Nonnull final BiFunction<T, U, Boolean> contentEqualFunction) {

        return equal(i1, i2, withNotNullChecks(contentEqualFunction));
    }

    public static <T extends HasID<ID>, U extends HasID<ID>, ID> boolean equalIdAndContent(
            @Nonnull final Iterable<T> i1,
            @Nonnull final Iterable<U> i2,
            @Nonnull final BiFunction<T, U, Boolean> contentEqualFunction) {

        return equal(i1, i2, withNotNullChecks(withIdCompare(contentEqualFunction)));
    }

}
