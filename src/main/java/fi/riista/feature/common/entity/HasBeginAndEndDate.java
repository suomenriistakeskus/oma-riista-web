package fi.riista.feature.common.entity;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.nullsLast;

import fi.riista.util.DateUtil;

import org.joda.time.LocalDate;
import org.joda.time.ReadablePartial;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.AssertTrue;

import java.util.Comparator;
import java.util.Objects;

public interface HasBeginAndEndDate {

    // Null begin/end date is interpreted as near-infinitely distant date.
    static Comparator<HasBeginAndEndDate> DEFAULT_COMPARATOR =
            comparing(HasBeginAndEndDate::getBeginDate, nullsFirst(naturalOrder()))
                    .thenComparing(HasBeginAndEndDate::getEndDate, nullsLast(naturalOrder()));

    LocalDate getBeginDate();

    LocalDate getEndDate();

    default boolean hasBeginDate() {
        return getBeginDate() != null;
    }

    default boolean hasEndDate() {
        return getEndDate() != null;
    }

    default boolean hasBeginAndEndDate() {
        return hasBeginDate() && hasEndDate();
    }

    default boolean hasBeginOrEndDate() {
        return hasBeginDate() || hasEndDate();
    }

    default boolean isBeginDateBefore(final ReadablePartial partial) {
        return hasBeginDate() && getBeginDate().isBefore(partial);
    }

    default boolean isBeginDateAfter(final ReadablePartial partial) {
        return hasBeginDate() && getBeginDate().isAfter(partial);
    }

    default boolean isEndDateBefore(final ReadablePartial partial) {
        return hasEndDate() && getEndDate().isBefore(partial);
    }

    default boolean isEndDateAfter(final ReadablePartial partial) {
        return hasEndDate() && getEndDate().isAfter(partial);
    }

    default boolean containsDate(@Nonnull final LocalDate date) {
        Objects.requireNonNull(date);
        return DateUtil.overlapsInclusive(getBeginDate(), getEndDate(), date);
    }

    default boolean isActiveNow() {
        return containsDate(DateUtil.today());
    }

    default boolean overlaps(@Nonnull final HasBeginAndEndDate other) {
        Objects.requireNonNull(other);
        return isActiveWithinPeriod(other.getBeginDate(), other.getEndDate());
    }

    default boolean isActiveWithinPeriod(@Nullable final LocalDate beginDate, @Nullable final LocalDate endDate) {
        return DateUtil.overlapsInclusive(getBeginDate(), getEndDate(), beginDate, endDate);
    }

    @AssertTrue
    default boolean isBeginEndDateOrderingValid() {
        return !hasBeginAndEndDate() || !getBeginDate().isAfter(getEndDate());
    }

}
