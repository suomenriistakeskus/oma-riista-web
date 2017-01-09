package fi.riista.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import fi.riista.feature.common.entity.HasID;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Filters {

    private Filters() {
        throw new AssertionError();
    }

    @Nonnull
    public static <T> Predicate<T> alwaysFalse() {
        return any -> false;
    }

    @Nonnull
    public static <T, U> Predicate<T> compose(
            @Nonnull final Predicate<U> predicate, @Nonnull final Function<? super T, ? extends U> function) {

        Objects.requireNonNull(predicate, "predicate is null");
        Objects.requireNonNull(function, "function is null");

        return object -> predicate.test(function.apply(object));
    }

    @Nonnull
    public static <T> Predicate<T> in(@Nonnull final Collection<T> collection) {
        return Predicates.in(collection)::apply;
    }

    @Nonnull
    public static <T> Predicate<T> notIn(@Nonnull final Collection<T> collection) {
        return in(collection).negate();
    }

    @Nonnull
    public static <ID> Predicate<HasID<ID>> idIn(@Nonnull final Collection<ID> ids) {
        return compose(in(ids), F::getId);
    }

    @Nonnull
    public static <ID> Predicate<HasID<ID>> idNotIn(@Nonnull final Collection<ID> ids) {
        return idIn(ids).negate();
    }

    @Nonnull
    public static <ID, T extends HasID<ID>> Predicate<HasID<ID>> hasAnyIdOf(@Nonnull final Iterable<T> objects) {
        return idIn(F.getUniqueIds(objects));
    }

    @Nonnull
    public static <ID, T extends HasID<ID>> Predicate<HasID<ID>> idNotAnyOf(@Nonnull final Iterable<T> objects) {
        return hasAnyIdOf(objects).negate();
    }

    @Nonnull
    public static <T, ID, U extends HasID<ID>> Predicate<T> hasRelationWithAny(
            @Nonnull final Function<? super T, U> function,
            @Nonnull final Iterable<? extends U> objects) {

        Objects.requireNonNull(function, "function is null");
        Objects.requireNonNull(objects, "objects is null");

        if (Iterables.isEmpty(objects)) {
            return alwaysFalse();
        }

        final Set<ID> ids = F.getUniqueIds(objects);

        return ids.isEmpty() ? alwaysFalse() : compose(in(ids), Functions.idOf(function));
    }

}
