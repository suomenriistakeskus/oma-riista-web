package fi.riista.feature.common.entity;

import fi.riista.util.DateUtil;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface Has2BeginEndDates extends HasBeginAndEndDate {

    void setBeginDate(LocalDate date);

    void setEndDate(LocalDate date);

    LocalDate getBeginDate2();

    void setBeginDate2(LocalDate date);

    LocalDate getEndDate2();

    void setEndDate2(LocalDate date);

    default boolean hasBeginDate2() {
        return getBeginDate2() != null;
    }

    default boolean hasEndDate2() {
        return getEndDate2() != null;
    }

    default boolean hasBeginOrEndDate2() {
        return hasBeginDate2() || hasEndDate2();
    }

    default boolean hasBeginAndEndDate2() {
        return hasBeginDate2() && hasEndDate2();
    }

    @Override
    default boolean containsDate(@Nonnull final LocalDate date) {
        Objects.requireNonNull(date);

        return hasBeginOrEndDate() && DateUtil.overlapsInclusive(getBeginDate(), getEndDate(), date) ||
                hasBeginOrEndDate2() && DateUtil.overlapsInclusive(getBeginDate2(), getEndDate2(), date);
    }

    default LocalDate getFirstDate() {
        return Optional.ofNullable(getBeginDate2())
                .map(beginDate2 -> {
                    final LocalDate beginDate = getBeginDate();
                    return beginDate.isBefore(beginDate2) ? beginDate : beginDate2;
                })
                .orElseGet(this::getBeginDate);
    }

    default LocalDate getLastDate() {
        return Optional.ofNullable(getEndDate2())
                .map(endDate2 -> {
                    final LocalDate endDate = getEndDate();
                    return endDate2.isAfter(endDate) ? endDate2 : endDate;
                })
                .orElseGet(this::getEndDate);
    }

    @AssertTrue
    default boolean isFirstDatePairPresent() {
        return hasBeginAndEndDate();
    }

    default boolean isOpenEnded() {
        return hasBeginDate() && !hasEndDate() ||
                !hasBeginDate() && hasEndDate() ||
                hasBeginDate2() && !hasEndDate2() ||
                !hasBeginDate2() && hasEndDate2();
    }

    @AssertTrue
    @Override
    default boolean isBeginEndDateOrderingValid() {
        return !(hasBeginAndEndDate() && getBeginDate().isAfter(getEndDate())) &&
                !(hasBeginAndEndDate2() && getBeginDate2().isAfter(getEndDate2()));
    }

    default void setDates(
            final LocalDate beginDate, final LocalDate endDate, final LocalDate beginDate2, final LocalDate endDate2) {

        setBeginDate(beginDate);
        setEndDate(endDate);
        setBeginDate2(beginDate2);
        setEndDate2(endDate2);
    }

    default void copyDatesFrom(@Nonnull final Has2BeginEndDates other) {
        Objects.requireNonNull(other);
        setDates(other.getBeginDate(), other.getEndDate(), other.getBeginDate2(), other.getEndDate2());
    }

    default String toString(@Nonnull final DateTimeFormatter dateFormatter) {
        Objects.requireNonNull(dateFormatter);

        return String.format("%s-%s, %s-%s", asStream()
                .map(date -> date == null ? null : dateFormatter.print(date))
                .map(StringUtils::trimToEmpty)
                .toArray());
    }

    default Stream<LocalDate> asStream() {
        return Stream.of(getBeginDate(), getEndDate(), getBeginDate2(), getEndDate2());
    }

    default IntStream collectClosedRangeHuntingYears() {
        return IntStream
                .concat(
                        DateUtil.streamYearsBetween(getBeginDate(), getEndDate()),
                        DateUtil.streamYearsBetween(getBeginDate2(), getEndDate2()))
                .sorted()
                .distinct();
    }

    default int resolveHuntingYear() {
        return findUnambiguousHuntingYear()
                .orElseThrow(() -> new IllegalStateException("Date ranges not within one hunting year"));
    }

    default OptionalInt findUnambiguousHuntingYear() {
        if (isOpenEnded()) {
            return OptionalInt.empty();
        }

        final int[] years = collectClosedRangeHuntingYears().toArray();
        return years.length == 1 ? OptionalInt.of(years[0]) : OptionalInt.empty();
    }

    static IntStream streamUniqueHuntingYearsSorted(final Stream<? extends Has2BeginEndDates> stream) {
        return stream.flatMapToInt(Has2BeginEndDates::collectClosedRangeHuntingYears).distinct().sorted();
    }

}
