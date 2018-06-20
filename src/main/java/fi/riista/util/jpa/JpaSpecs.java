package fi.riista.util.jpa;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.EntityLifecycleFields_;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.common.entity.LifecycleEntity_;
import fi.riista.util.DateUtil;
import io.vavr.Function3;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class JpaSpecs {

    private JpaSpecs() {
        throw new AssertionError();
    }

    @Nonnull
    public static <T> Specification<T> conjunction() {
        return (root, query, cb) -> cb.conjunction();
    }

    @Nonnull
    public static <T> Specification<T> disjunction() {
        return (root, query, cb) -> cb.disjunction();
    }

    @Nonnull
    public static <ID extends Serializable, T extends HasID<ID>> Specification<T> withId(@Nonnull final ID id) {
        Objects.requireNonNull(id);
        return (root, query, cb) -> cb.equal(root.get(root.getModel().getId(id.getClass())), id);
    }

    @Nonnull
    public static <ID extends Serializable, T extends HasID<ID>> Specification<T> withIds(
            @Nullable final Collection<ID> ids) {

        return (root, query, cb) -> JpaPreds.idInCollection(cb, root, ids);
    }

    @Nonnull
    public static <ID extends Serializable, T extends HasID<ID>> Specification<T> withId(
            @Nonnull final SingularAttribute<? super T, ID> attribute, @Nonnull final ID id) {

        Objects.requireNonNull(attribute, "attribute must not be null");
        Objects.requireNonNull(id, "id must not be null");

        return (root, query, cb) -> cb.equal(root.get(attribute), id);
    }

    @Nonnull
    public static <T> Specification<T> isNull(@Nonnull final SingularAttribute<? super T, ?> attribute) {

        Objects.requireNonNull(attribute);

        return (root, query, cb) -> cb.isNull(root.get(attribute));
    }

    @Nonnull
    public static <T> Specification<T> isNotNull(@Nonnull final SingularAttribute<? super T, ?> attribute) {

        Objects.requireNonNull(attribute);

        return (root, query, cb) -> cb.isNotNull(root.get(attribute));
    }

    @Nonnull
    public static <T, U> Specification<T> equal(
            @Nonnull final SingularAttribute<? super T, U> attribute, @Nullable final U value) {

        Objects.requireNonNull(attribute, "attribute must not be null");

        return (root, query, cb) -> JpaPreds.equal(cb, root.get(attribute), value);
    }

    @Nonnull
    public static <T, X, Y> Specification<T> equal(
            @Nonnull final SingularAttribute<? super T, X> attribute1,
            @Nonnull final SingularAttribute<? super X, Y> attribute2,
            @Nullable final Y value) {

        Objects.requireNonNull(attribute1, "attribute1 must not be null");
        Objects.requireNonNull(attribute2, "attribute2 must not be null");

        return (root, query, cb) -> JpaPreds.equal(cb, root.join(attribute1).get(attribute2), value);
    }

    @Nonnull
    public static <T, X, Y> Specification<T> equal(
            @Nonnull final PluralAttribute<? super T, ?, X> attribute1,
            @Nonnull final SingularAttribute<? super X, Y> attribute2,
            @Nullable final Y value) {

        Objects.requireNonNull(attribute1, "attribute1 must not be null");
        Objects.requireNonNull(attribute2, "attribute2 must not be null");

        return (root, query, cb) -> JpaPreds.equal(cb, CriteriaUtils.join(root, attribute1).get(attribute2), value);
    }

    @Nonnull
    public static <T, X, Y, Z> Specification<T> equal(
            @Nonnull final SingularAttribute<? super T, X> attribute1,
            @Nonnull final SingularAttribute<? super X, Y> attribute2,
            @Nonnull final SingularAttribute<? super Y, Z> attribute3,
            @Nullable final Z value) {

        Objects.requireNonNull(attribute1, "attribute1 must not be null");
        Objects.requireNonNull(attribute2, "attribute2 must not be null");
        Objects.requireNonNull(attribute3, "attribute3 must not be null");

        return (root, query, cb) -> JpaPreds.equal(cb, root.join(attribute1).join(attribute2).get(attribute3), value);
    }

    @Nonnull
    public static <T, U> Specification<T> notEqual(
            @Nonnull final SingularAttribute<? super T, U> attribute, @Nullable final U value) {

        Objects.requireNonNull(attribute, "attribute must not be null");

        return (root, query, cb) -> {
            final Path<U> path = root.get(attribute);
            return value == null ? cb.isNotNull(path) : cb.or(cb.isNull(path), cb.notEqual(path, value));
        };
    }

    @Nonnull
    public static <T, U> Specification<T> isNotEmpty(@Nonnull final PluralAttribute<T, java.util.Set<U>, U> attribute) {

        Objects.requireNonNull(attribute);

        return (root, query, cb) -> cb.isNotEmpty(root.get(attribute));
    }

    @Nonnull
    public static <T, U> Specification<T> inCollection(
            @Nonnull final SingularAttribute<? super T, U> attribute, @Nullable final Collection<? extends U> values) {

        Objects.requireNonNull(attribute, "attribute must not be null");

        return (root, query, cb) -> JpaPreds.inCollection(cb, root.get(attribute), values);
    }

    @Nonnull
    @SafeVarargs
    public static <T, U> Specification<T> inArray(
            @Nonnull final SingularAttribute<? super T, U> attribute, @Nullable final U... values) {

        Objects.requireNonNull(attribute, "attribute must not be null");

        return inCollection(attribute, values != null ? Arrays.asList(values) : Collections.<U> emptyList());
    }

    @Nonnull
    public static <T, U> Specification<T> inCollection(
            @Nonnull final SetAttribute<? super T, U> setAttribute, @Nullable final Collection<U> values) {

        Objects.requireNonNull(setAttribute, "setAttribute must not be null");

        return (root, query, cb) -> JpaPreds.inCollection(cb, root.join(setAttribute), values);
    }

    @Nonnull
    public static <T, ID extends Serializable, U extends HasID<ID>> Specification<T> inIdCollection(
            @Nonnull final SingularAttribute<? super T, U> attribute,
            @Nonnull final SingularAttribute<? super U, ID> idAttribute,
            @Nullable final Collection<ID> values) {

        Objects.requireNonNull(attribute, "attribute must not be null");
        Objects.requireNonNull(idAttribute, "idAttribute must not be null");

        return (root, query, cb) -> JpaPreds.inCollection(cb, root.get(attribute).get(idAttribute), values);
    }

    @Nonnull
    public static <T, ID extends Serializable, U extends HasID<ID>> Specification<T> hasRelationWithId(
            @Nonnull final SingularAttribute<? super T, U> entityAttribute,
            @Nonnull final SingularAttribute<? super U, ID> idAttribute,
            @Nonnull final ID id) {

        Objects.requireNonNull(entityAttribute, "entityAttribute must not be null");
        Objects.requireNonNull(idAttribute, "idAttribute must not be null");
        Objects.requireNonNull(id, "id must not be null");

        return (root, query, cb) -> cb.equal(root.get(entityAttribute).get(idAttribute), id);
    }

    @Nonnull
    public static <T, U, ID extends Serializable, V extends HasID<ID>> Specification<T> joinPathToId(
            @Nonnull final SingularAttribute<? super T, U> entityAttribute1,
            @Nonnull final SingularAttribute<? super U, V> entityAttribute2,
            @Nonnull final SingularAttribute<? super V, ID> idAttribute,
            @Nonnull final ID id) {

        Objects.requireNonNull(entityAttribute1, "entityAttribute1 must not be null");
        Objects.requireNonNull(entityAttribute2, "entityAttribute2 must not be null");
        Objects.requireNonNull(idAttribute, "idAttribute must not be null");
        Objects.requireNonNull(id, "id must not be null");

        return (root, query, cb) -> cb.equal(root.join(entityAttribute1).get(entityAttribute2).get(idAttribute), id);
    }

    @Nonnull
    public static <T, U, V> Specification<U> pathToValueExists(
            @Nonnull final SingularAttribute<T, U> entityAttribute1,
            @Nonnull final SingularAttribute<? super T, V> entityAttribute2,
            @Nonnull final V value) {

        Objects.requireNonNull(entityAttribute1, "entityAttribute1 must not be null");
        Objects.requireNonNull(entityAttribute2, "entityAttribute2 must not be null");
        Objects.requireNonNull(value, "value must not be null");

        return JpaSubQuery.inverseOf(entityAttribute1)
                .exists((root, cb) -> cb.equal(root.get(entityAttribute2), value));
    }

    @Nonnull
    public static <T, U, ID extends Serializable, V extends HasID<ID>> Specification<U> pathToIdExists(
            @Nonnull final SingularAttribute<T, U> entityAttribute1,
            @Nonnull final SingularAttribute<? super T, V> entityAttribute2,
            @Nonnull final SingularAttribute<? super V, ID> idAttribute,
            @Nonnull final ID id) {

        Objects.requireNonNull(entityAttribute1, "entityAttribute1 must not be null");
        Objects.requireNonNull(entityAttribute2, "entityAttribute2 must not be null");
        Objects.requireNonNull(idAttribute, "idAttribute must not be null");
        Objects.requireNonNull(id, "id must not be null");

        return JpaSubQuery.inverseOf(entityAttribute1)
                .exists((root, cb) -> cb.equal(root.get(entityAttribute2).get(idAttribute), id));
    }

    @Nonnull
    public static <T, U, V, ID extends Serializable, X extends HasID<ID>> Specification<U> pathToIdExists(
            @Nonnull final SingularAttribute<T, U> entityAttribute1,
            @Nonnull final SingularAttribute<? super T, V> entityAttribute2,
            @Nonnull final SingularAttribute<? super V, X> entityAttribute3,
            @Nonnull final SingularAttribute<? super X, ID> idAttribute,
            @Nonnull final ID id) {

        Objects.requireNonNull(entityAttribute1, "entityAttribute1 must not be null");
        Objects.requireNonNull(entityAttribute2, "entityAttribute2 must not be null");
        Objects.requireNonNull(entityAttribute3, "entityAttribute3 must not be null");
        Objects.requireNonNull(idAttribute, "idAttribute must not be null");
        Objects.requireNonNull(id, "id must not be null");

        return JpaSubQuery.inverseOf(entityAttribute1)
                .exists((root, cb) -> cb.equal(root.join(entityAttribute2).get(entityAttribute3).get(idAttribute), id));
    }

    @Nonnull
    public static <T> Specification<T> withinInterval(
            @Nonnull final SingularAttribute<? super T, Date> dateAttribute, @Nonnull final Interval interval) {

        Objects.requireNonNull(dateAttribute, "dateAttribute must not be null");
        Objects.requireNonNull(interval, "interval must not be null");

        return (root, query, cb) -> JpaPreds.withinInterval(cb, root.get(dateAttribute), interval);
    }

    @Nonnull
    public static <T> Specification<T> withinInterval(
            @Nonnull final SingularAttribute<? super T, Date> dateAttribute,
            @Nullable final LocalDate beginDate,
            @Nullable final LocalDate endDate) {

        Objects.requireNonNull(dateAttribute, "dateAttribute must not be null");

        return (root, query, cb) -> JpaPreds.withinInterval(cb, root.get(dateAttribute), beginDate, endDate);
    }

    @Nonnull
    public static <T> Specification<T> withinInterval(
            @Nonnull final SingularAttribute<? super T, LocalDate> beginDateAttribute,
            @Nonnull final SingularAttribute<? super T, LocalDate> endDateAttribute,
            @Nonnull final LocalDate dateOfInterest) {

        Objects.requireNonNull(beginDateAttribute, "beginDateAttribute must not be null");
        Objects.requireNonNull(endDateAttribute, "endDateAttribute must not be null");
        Objects.requireNonNull(dateOfInterest, "dateOfInterest must not be null");

        return (root, query, cb) -> JpaPreds.withinInterval(
                cb, root.get(beginDateAttribute), root.get(endDateAttribute), dateOfInterest);
    }

    @Nonnull
    public static <T> Specification<T> overlapsInterval(
            @Nonnull final SingularAttribute<? super T, LocalDate> dateAttribute,
            @Nonnull final Interval interval) {

        Objects.requireNonNull(dateAttribute, "dateAttribute must not be null");
        Objects.requireNonNull(interval, "interval must not be null");

        return (root, query, cb) -> JpaPreds.overlapsInterval(cb, root.get(dateAttribute), interval);
    }

    @Nonnull
    public static <T> Specification<T> overlapsInterval(
            @Nonnull final SingularAttribute<? super T, LocalDate> beginDateAttribute,
            @Nonnull final SingularAttribute<? super T, LocalDate> endDateAttribute,
            @Nullable final DateTime beginTime,
            @Nullable final DateTime endTime) {

        return (root, query, cb) -> JpaPreds.overlapsInterval(
                cb, root.get(beginDateAttribute), root.get(endDateAttribute), beginTime, endTime);
    }

    @Nonnull
    public static <T> Specification<T> withinHuntingYear(
            @Nonnull final SingularAttribute<? super T, Date> dateAttribute, final int firstCalendarYearOfHuntingYear) {

        return withinInterval(dateAttribute, DateUtil.huntingYearInterval(firstCalendarYearOfHuntingYear));
    }

    @Nonnull
    public static <T extends LifecycleEntity<? extends Serializable>> Specification<T> creationTimeOlderThan(
            @Nonnull final Date olderThan) {

        Objects.requireNonNull(olderThan);

        return (root, query, cb) -> {
            final Path<Date> dateField =
                    root.get(LifecycleEntity_.lifecycleFields).get(EntityLifecycleFields_.creationTime);
            return cb.lessThan(dateField, olderThan);
        };
    }

    @Nonnull
    public static <T extends LifecycleEntity<? extends Serializable>> Specification<T> creationTimeBetween(
            @Nonnull final Date start, @Nonnull final Date end) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        return (root, query, cb) -> {
            final Path<Date> dateField =
                    root.get(LifecycleEntity_.lifecycleFields).get(EntityLifecycleFields_.creationTime);
            return cb.between(dateField, start, end);
        };
    }

    @Nonnull
    public static <T extends LifecycleEntity<? extends Serializable>> Specification<T> dateFieldBefore(
            @Nonnull final SingularAttribute<? super T, DateTime> dateAttribute, @Nonnull final DateTime before) {

        Objects.requireNonNull(dateAttribute, "dateAttribute must not be null");
        Objects.requireNonNull(before, "before must not be null");

        return (root, query, cb) -> cb.lessThan(root.get(dateAttribute), before);
    }

    @Nonnull
    public static <T, U> Specification<T> fetch(@Nonnull final SingularAttribute<? super T, U> attribute) {
        return fetch(attribute, null);
    }

    @Nonnull
    public static <T, U> Specification<T> fetch(
            @Nonnull final SingularAttribute<? super T, U> attribute, @Nullable final JoinType joinType) {

        Objects.requireNonNull(attribute, "attribute must not be null");

        return (root, query, cb) -> {
            if (joinType != null) {
                root.fetch(attribute, joinType);
            } else {
                root.fetch(attribute);
            }
            return cb.and();
        };
    }

    @Nonnull
    public static <T> Specification<T> likeIgnoreCase(
            @Nonnull final SingularAttribute<? super T, String> attribute, @Nonnull final String value) {

        Objects.requireNonNull(attribute, "attribute must not be null");
        Objects.requireNonNull(value, "value must not be null");

        return (root, query, cb) -> JpaPreds.containsLikeIgnoreCase(cb, root.get(attribute), value);
    }

    @Nonnull
    public static <T extends LifecycleEntity<? extends Serializable>> Specification<T> notSoftDeleted() {
        return (root, query, cb) -> JpaPreds.notSoftDeleted(cb, root);
    }

    @Nonnull
    public static <T> Specification<T> and(@Nonnull final Collection<? extends Specification<T>> specs) {
        Objects.requireNonNull(specs);
        Preconditions.checkArgument(specs.size() > 0, "At least one specification must be given");
        return reduceConjunction(specs::stream);
    }

    @Nonnull
    @SafeVarargs
    public static <T> Specification<T> and(@Nonnull final Specification<T>... specs) {
        Objects.requireNonNull(specs);
        Preconditions.checkArgument(specs.length > 0, "At least one specification must be given");
        return specs.length == 1 ? specs[0] : reduceConjunction(() -> Arrays.stream(specs));
    }

    @Nonnull
    public static <T> Specification<T> or(@Nonnull final Collection<? extends Specification<T>> specs) {
        Objects.requireNonNull(specs);
        Preconditions.checkArgument(specs.size() > 0, "At least one specification must be given");
        return reduceDisjunction(specs::stream);
    }

    @Nonnull
    @SafeVarargs
    public static <T> Specification<T> or(@Nonnull final Specification<T>... specs) {
        Objects.requireNonNull(specs);
        Preconditions.checkArgument(specs.length > 0, "At least one specification must be given");
        return specs.length == 1 ? specs[0] : reduceDisjunction(() -> Arrays.stream(specs));
    }

    private static <T> Specification<T> reduceConjunction(final Supplier<Stream<? extends Specification<T>>> streamSupplier) {
        return reduce(streamSupplier, (first, second, cb) -> cb.and(first, second));
    }

    private static <T> Specification<T> reduceDisjunction(final Supplier<Stream<? extends Specification<T>>> streamSupplier) {
        return reduce(streamSupplier, (first, second, cb) -> cb.or(first, second));
    }

    private static <T> Specification<T> reduce(final Supplier<Stream<? extends Specification<T>>> streamSupplier,
                                               final Function3<Predicate, Predicate, CriteriaBuilder, Predicate> reducer) {

        return (root, query, cb) -> streamSupplier.get()
                .map(spec -> spec.toPredicate(root, query, cb))
                .reduce((p1, p2) -> reducer.apply(p1, p2, cb))
                .get();
    }

}
