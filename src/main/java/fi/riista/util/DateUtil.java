package fi.riista.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Range;
import fi.riista.config.Constants;
import javaslang.Tuple;
import javaslang.Tuple2;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public final class DateUtil {

    public static final int HUNTING_YEAR_BEGIN_MONTH = 8;

    public static final String DATE_FORMAT_FINNISH = "d.M.yyyy";

    public static final String TIMESTAMP_FORMAT_WITH_OFFSET_ZONE = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private static final Splitter DASH_SPLITTER = Splitter.on('-').trimResults();

    public static DateTime now() {
        return DateTime.now(Constants.DEFAULT_TIMEZONE);
    }

    public static LocalDateTime localDateTime() {
        return LocalDateTime.now(Constants.DEFAULT_TIMEZONE);
    }

    public static LocalDate today() {
        return LocalDate.now(Constants.DEFAULT_TIMEZONE);
    }

    @Nonnull
    public static Duration toDuration(@Nullable final LocalDate startDate, @Nullable final LocalTime startTime,
                                      @Nullable final LocalDate endDate, @Nullable final LocalTime endTime) {

        return startDate == null || startTime == null || endDate == null || endTime == null ? new Duration(0)
                : new Duration(toDateTimeNullSafe(startDate, startTime), toDateTimeNullSafe(endDate, endTime));
    }

    @Nullable
    public static DateTime toDateTimeNullSafe(@Nullable final LocalDate date) {
        return date == null ? null : toDateTime(date);
    }

    @Nullable
    public static DateTime toDateTimeNullSafe(@Nullable final LocalDate date, @Nullable final LocalTime time) {
        return date == null || time == null ? null : toDateTime(date.toLocalDateTime(time));
    }

    @Nullable
    public static LocalDate toLocalDateNullSafe(@Nullable final Date date) {
        return date == null ? null : new LocalDate(date, Constants.DEFAULT_TIMEZONE);
    }

    @Nullable
    public static LocalDateTime toLocalDateTimeNullSafe(@Nullable final Date date) {
        return date == null ? null : new LocalDateTime(date, Constants.DEFAULT_TIMEZONE);
    }

    @Nullable
    public static Date toDateNullSafe(@Nullable final LocalDate localDate) {
        return localDate == null ? null : toDateTime(localDate).toDate();
    }

    @Nullable
    public static Date toDateNullSafe(@Nullable final LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.toDateTime(Constants.DEFAULT_TIMEZONE).toDate();
    }

    @Nullable
    public static Date toDateTodayNullSafe(@Nullable final LocalTime localTime) {
        return localTime == null ? null : localTime.toDateTimeToday(Constants.DEFAULT_TIMEZONE).toDate();
    }

    @Nonnull
    public static Period calculateAge(@Nonnull final LocalDate beginDate,
                                      @Nonnull final DateTime endDateTime) {
        final DateTime start = beginDate.toDateTimeAtStartOfDay();

        return new Duration(start, endDateTime).toPeriodFrom(start, PeriodType.years());
    }

    public static boolean isAdultBirthDate(@Nonnull final LocalDate dateOfBirth) {
        return DateUtil.calculateAge(dateOfBirth, DateUtil.now()).getYears() >= 18;
    }

    @Nonnull
    public static LocalDate huntingYearBeginDate(final int beginYear) {
        return new LocalDate(beginYear, HUNTING_YEAR_BEGIN_MONTH, 1);
    }

    @Nonnull
    public static LocalDate huntingYearEndDate(final int beginYear) {
        return new LocalDate(beginYear + 1, HUNTING_YEAR_BEGIN_MONTH, 1).minusDays(1);
    }

    public static int getFirstCalendarYearOfCurrentHuntingYear() {
        return getFirstCalendarYearOfHuntingYearContaining(today());
    }

    public static Tuple2<LocalDate, LocalDate> parseDateInterval(
            final String dates, final DateTimeFormatter dateTimeFormatter) {

        if (StringUtils.isBlank(dates)) {
            return null;
        }

        final List<String> parts = DASH_SPLITTER.splitToList(dates.replaceAll("\\s", ""));

        if (parts.size() != 2) {
            throw new IllegalArgumentException("Invalid range");
        }

        final LocalDate begin = dateTimeFormatter.parseLocalDate(parts.get(0));
        final LocalDate end = dateTimeFormatter.parseLocalDate(parts.get(1));

        return Tuple.of(begin, end);
    }

    @Nonnull
    public static Interval toInterval(@Nonnull final LocalDate date) {
        return Objects.requireNonNull(date).toInterval(Constants.DEFAULT_TIMEZONE);
    }

    @Nonnull
    public static Interval toDateInterval(@Nonnull final DateTime datetime) {
        return Objects.requireNonNull(datetime).toLocalDate().toInterval(Constants.DEFAULT_TIMEZONE);
    }

    @Nonnull
    public static Interval createDateInterval(final LocalDate startDate, final LocalDate endDate) {
        Objects.requireNonNull(startDate, "no startDate");
        Objects.requireNonNull(endDate, "no endDate");

        Preconditions.checkArgument(!startDate.isAfter(endDate), "startDate must not be greater than endDate");

        return new Interval(toDateTime(startDate), toDateTime(endDate.plusDays(1)));
    }

    @Nonnull
    public static Interval huntingYearInterval(final int beginYear) {
        return createDateInterval(huntingYearBeginDate(beginYear), huntingYearEndDate(beginYear));
    }

    public static int getFirstCalendarYearOfHuntingYearContaining(@Nonnull final LocalDate date) {
        final int year = Objects.requireNonNull(date, "date must not be null").getYear();
        return date.getMonthOfYear() >= HUNTING_YEAR_BEGIN_MONTH ? year : year - 1;
    }

    public static boolean overlapsInclusive(
            @Nullable final Date begin, @Nullable final Date end, @Nonnull final LocalDate date) {

        return overlapsInclusive(toLocalDateNullSafe(begin), toLocalDateNullSafe(end), date);
    }

    public static boolean overlapsInclusive(
            @Nullable final LocalDate begin, @Nullable final LocalDate end, @Nonnull final LocalDate date) {

        Objects.requireNonNull(date, "date must not be null");

        return begin == null && end == null || dateRange(begin, end).contains(date);
    }

    public static boolean overlapsInclusive(
            @Nullable final LocalDate begin,
            @Nullable final LocalDate end,
            @Nullable final LocalDate begin2,
            @Nullable final LocalDate end2) {

        return begin == null && end == null ||
                begin2 == null && end2 == null ||
                dateRange(begin, end).isConnected(dateRange(begin2, end2));
    }

    private static Range<LocalDate> dateRange(@Nullable final LocalDate begin, @Nullable final LocalDate end) {
        if (begin == null && end == null) {
            return Range.all();
        }
        if (begin == null) {
            return Range.atMost(end);
        }
        if (end == null) {
            return Range.atLeast(begin);
        }
        return begin.isBefore(end) ? Range.closed(begin, end) : Range.closed(end, begin);
    }

    private static DateTime toDateTime(final LocalDate date) {
        return date.toDateTimeAtStartOfDay(Constants.DEFAULT_TIMEZONE);
    }

    private static DateTime toDateTime(final LocalDateTime date) {
        return date.toDateTime(Constants.DEFAULT_TIMEZONE);
    }

    private DateUtil() {
        throw new AssertionError();
    }

    @Nonnull
    public static IntStream huntingYearsBetween(@Nonnull final LocalDate beginDate,
                                                @Nonnull final LocalDate endDate) {

        Objects.requireNonNull(beginDate, "beginDate must not be null");
        Objects.requireNonNull(endDate, "endDate must not be null");

        return streamYearsBetween_notNullSafe(beginDate, endDate);
    }

    @Nonnull
    public static IntStream streamYearsBetween(@Nullable final LocalDate beginDate, @Nullable final LocalDate endDate) {
        return F.anyNull(beginDate, endDate) ? IntStream.empty() : streamYearsBetween_notNullSafe(beginDate, endDate);
    }

    private static IntStream streamYearsBetween_notNullSafe(final LocalDate beginDate, final LocalDate endDate) {
        return IntStream.rangeClosed(
                getFirstCalendarYearOfHuntingYearContaining(beginDate),
                getFirstCalendarYearOfHuntingYearContaining(endDate));
    }

}
