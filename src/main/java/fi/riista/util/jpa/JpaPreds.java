package fi.riista.util.jpa;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.EntityLifecycleFields_;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.common.entity.LifecycleEntity_;
import fi.riista.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

public final class JpaPreds {

    @Nonnull
    public static <T> Predicate equal(
            @Nonnull final CriteriaBuilder cb, @Nonnull final Path<T> path, @Nullable final T value) {

        Objects.requireNonNull(cb, "cb must not be null");
        Objects.requireNonNull(path, "path must not be null");

        return value == null ? cb.isNull(path) : cb.equal(path, value);
    }

    @Nonnull
    public static <T> Predicate inCollection(
            @Nonnull final CriteriaBuilder cb,
            @Nonnull final Expression<T> expr,
            @Nullable final Collection<? extends T> values) {

        Objects.requireNonNull(cb, "cb must not be null");
        Objects.requireNonNull(expr, "expr must not be null");

        return CollectionUtils.isEmpty(values) ? cb.disjunction() : expr.in(cb.literal(values));
    }

    @Nonnull
    public static <ID extends Serializable, T extends HasID<ID>> Predicate idInCollection(
            @Nonnull final CriteriaBuilder cb, @Nonnull final Root<T> root, @Nullable final Collection<ID> ids) {

        Objects.requireNonNull(cb, "cb must not be null");
        Objects.requireNonNull(root, "root must not be null");

        if (CollectionUtils.isEmpty(ids)) {
            return cb.disjunction();
        }

        final EntityType<T> rootModel = root.getModel();

        if (!rootModel.hasSingleIdAttribute()) {
            throw new IllegalArgumentException(
                    "Single-column-identified entity expected but not received: " + rootModel.getJavaType().getName());
        }

        @SuppressWarnings("unchecked")
        final Class<ID> idClass = (Class<ID>) rootModel.getIdType().getJavaType();

        return root.get(root.getModel().getId(idClass)).in(cb.literal(ids));
    }

    @Nonnull
    public static <T extends LifecycleEntity<? extends Serializable>> Predicate notSoftDeleted(
            @Nonnull final CriteriaBuilder cb, @Nonnull final From<?, T> from) {

        Objects.requireNonNull(cb, "cb must not be null");
        Objects.requireNonNull(from, "path must not be null");

        return cb.isNull(from.get(LifecycleEntity_.lifecycleFields).get(EntityLifecycleFields_.deletionTime));
    }

    @Nonnull
    public static Predicate withinInterval(
            @Nonnull final CriteriaBuilder cb, @Nonnull final Path<DateTime> path, @Nonnull final Interval interval) {

        Objects.requireNonNull(interval, "interval must not be null");

        return withinInterval(cb, path, interval.getStart(), interval.getEnd());
    }

    @Nonnull
    public static Predicate withinInterval(
            @Nonnull final CriteriaBuilder cb,
            @Nonnull final Path<DateTime> path,
            @Nullable final LocalDate beginDate,
            @Nullable final LocalDate endDate) {

        DateTime _endDate = null;

        if (endDate != null) {
            if (beginDate != null) {
                Preconditions.checkArgument(!beginDate.isAfter(endDate), "beginDate must not be after endDate");
            }

            _endDate = DateUtil.toDateTimeNullSafe(endDate.plusDays(1));
        }

        final DateTime _beginDate = DateUtil.toDateTimeNullSafe(beginDate);

        return withinInterval(cb, path, _beginDate, _endDate);
    }

    @Nonnull
    public static Predicate withinInterval(
            @Nonnull final CriteriaBuilder cb,
            @Nonnull final Path<DateTime> path,
            @Nullable final DateTime beginDate,
            @Nullable final DateTime endDate) {

        Objects.requireNonNull(cb, "cb must not be null");
        Objects.requireNonNull(path, "path must not be null");

        final Predicate dateNotNull = cb.isNotNull(path);

        if (beginDate == null && endDate == null) {
            return dateNotNull;
        }
        if (beginDate == null) {
            return cb.and(dateNotNull, cb.lessThan(path, endDate));
        }
        if (endDate == null) {
            return cb.and(dateNotNull, cb.greaterThanOrEqualTo(path, beginDate));
        }

        return cb.and(dateNotNull, cb.greaterThanOrEqualTo(path, beginDate), cb.lessThan(path, endDate));
    }

    @Nonnull
    public static Predicate withinInterval(
            @Nonnull final CriteriaBuilder cb,
            @Nonnull final Path<LocalDate> beginDate,
            @Nonnull final Path<LocalDate> endDate,
            @Nonnull final LocalDate dateOfInterest) {

        Objects.requireNonNull(cb, "cb must not be null");
        Objects.requireNonNull(beginDate, "beginDate must not be null");
        Objects.requireNonNull(endDate, "endDate must not be null");
        Objects.requireNonNull(dateOfInterest, "dateOfInterest must not be null");

        return cb.or(
                cb.and(cb.isNull(beginDate), cb.or(cb.isNull(endDate), cb.greaterThanOrEqualTo(endDate, dateOfInterest))),
                cb.and(cb.isNull(endDate), cb.lessThanOrEqualTo(beginDate, dateOfInterest)),
                cb.and(cb.lessThanOrEqualTo(beginDate, dateOfInterest), cb.greaterThanOrEqualTo(endDate, dateOfInterest))
        );
    }

    @Nonnull
    public static Predicate overlapsInterval(
            @Nonnull final CriteriaBuilder cb,
            @Nonnull final Path<LocalDate> path,
            @Nonnull final Interval interval) {

        Objects.requireNonNull(cb, "cb must not be null");
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(interval, "interval must not be null");

        return overlapsInterval(cb, path, interval.getStart(), interval.getEnd());
    }

    @Nonnull
    public static Predicate overlapsInterval(
            @Nonnull final CriteriaBuilder cb,
            @Nonnull final Path<LocalDate> path,
            @Nullable final DateTime beginTime,
            @Nullable final DateTime endTime) {

        Objects.requireNonNull(cb, "cb must not be null");
        Objects.requireNonNull(path, "path must not be null");

        final Predicate dateNotNull = cb.isNotNull(path);

        if (beginTime == null && endTime == null) {
            return dateNotNull;
        }
        if (beginTime == null) {
            return cb.and(dateNotNull, cb.lessThan(path, roundToStartOfNextDayIfNotStartOfDay(endTime).toLocalDate()));
        }
        if (endTime == null) {
            return cb.and(dateNotNull, cb.greaterThanOrEqualTo(path, beginTime.toLocalDate()));
        }

        Preconditions.checkArgument(beginTime.isBefore(endTime), "beginTime must be before endTime");

        return cb.and(
                dateNotNull,
                cb.greaterThanOrEqualTo(path, beginTime.toLocalDate()),
                cb.lessThan(path, roundToStartOfNextDayIfNotStartOfDay(endTime).toLocalDate()));
    }

    @Nonnull
    public static Predicate overlapsInterval(
            @Nonnull final CriteriaBuilder cb,
            @Nonnull final Path<LocalDate> beginDate,
            @Nonnull final Path<LocalDate> endDate,
            @Nullable final DateTime beginTime,
            @Nullable final DateTime endTime) {

        Objects.requireNonNull(cb, "cb must not be null");
        Objects.requireNonNull(beginDate, "beginDate must not be null");
        Objects.requireNonNull(endDate, "endDate must not be null");

        if (beginTime == null && endTime == null) {
            return cb.and();
        }

        final LocalDate beginDateToCompare = beginTime == null ? null : beginTime.toLocalDate();
        final LocalDate endDateToCompare =
                endTime == null ? null : roundToStartOfNextDayIfNotStartOfDay(endTime).toLocalDate();

        final Predicate nullBeginDate = cb.isNull(beginDate);
        final Predicate nullEndDate = cb.isNull(endDate);

        if (beginDateToCompare == null) {
            return cb.or(nullBeginDate, cb.lessThan(beginDate, endDateToCompare));
        }
        if (endDateToCompare == null) {
            return cb.or(nullEndDate, cb.greaterThanOrEqualTo(endDate, beginDateToCompare));
        }

        return cb.and(
                cb.or(nullBeginDate, cb.lessThan(beginDate, endDateToCompare)),
                cb.or(nullEndDate, cb.greaterThanOrEqualTo(endDate, beginDateToCompare)));
    }

    @Nonnull
    public static Predicate withinHuntingYear(
            @Nonnull final Path<Date> date,
            @Nonnull final Expression<Integer> huntingYear,
            @Nonnull final CriteriaBuilder cb) {

        final Expression<Integer> year = cb.function("year", Integer.class, date);
        final Expression<Integer> month = cb.function("month", Integer.class, date);

        final Predicate currentHuntingYearBeganLastYear = cb.lessThan(month, DateUtil.HUNTING_YEAR_BEGIN_MONTH);

        return cb.and(
                cb.isNotNull(date),
                cb.isNotNull(huntingYear),
                cb.or(
                        cb.and(cb.equal(year, huntingYear), cb.not(currentHuntingYearBeganLastYear)),
                        cb.and(cb.equal(year, cb.sum(huntingYear, 1)), currentHuntingYearBeganLastYear)));
    }

    @Nonnull
    public static Predicate containsLikeIgnoreCase(
            @Nonnull final CriteriaBuilder cb, @Nonnull final Path<String> path, @Nonnull final String value) {

        return likeIgnoreCaseInternal(cb, path, "%" + value.toLowerCase() + "%");
    }

    @Nonnull
    public static Predicate beginsWithIgnoreCase(
            @Nonnull final CriteriaBuilder cb, @Nonnull final Path<String> path, @Nonnull final String value) {

        return likeIgnoreCaseInternal(cb, path, value.toLowerCase() + "%");
    }

    private static Predicate likeIgnoreCaseInternal(
            final CriteriaBuilder cb, final Path<String> path, final String value) {

        Objects.requireNonNull(cb, "cb must not be null");
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(value, "value must not be null");

        return cb.like(cb.lower(path), value);
    }

    private static DateTime roundToStartOfNextDayIfNotStartOfDay(final DateTime dt) {
        final DateTime startOfDay = dt.withTimeAtStartOfDay();
        return dt.isAfter(startOfDay) ? dt.plusDays(1) : dt;
    }

    private JpaPreds() {
        throw new AssertionError();
    }

}
