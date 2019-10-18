package fi.riista.util;

import com.google.common.collect.Range;
import fi.riista.config.Constants;
import io.vavr.Tuple;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import static fi.riista.test.TestUtils.dt;
import static fi.riista.test.TestUtils.i;
import static fi.riista.test.TestUtils.ld;
import static fi.riista.util.DateUtil.HUNTING_YEAR_BEGIN_MONTH;
import static fi.riista.util.DateUtil.now;
import static fi.riista.util.DateUtil.today;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DateUtilTest {

    @Test
    public void testCalculateAge() {
        final LocalDate today = today();

        assertEquals(DateUtil.calculateAge(today.minusYears(30), now()).getYears(), 30);
        assertEquals(DateUtil.calculateAge(today.minusYears(30).minusDays(1), now()).getYears(), 30);
        assertEquals(DateUtil.calculateAge(today.minusYears(30).plusDays(1), now()).getYears(), 29);
    }

    @Test
    public void testIsAdultBirthDate() {
        final LocalDate today = today();

        assertFalse(DateUtil.isAdultBirthDate(today));
        assertTrue(DateUtil.isAdultBirthDate(ld(1970, 1, 1)));
        assertTrue(DateUtil.isAdultBirthDate(today.minusYears(18).minusDays(1)));
        assertTrue(DateUtil.isAdultBirthDate(today.minusYears(18)));
        assertFalse(DateUtil.isAdultBirthDate(today.minusYears(18).plusDays(1)));
    }

    @Test
    public void testHuntingYearContaining() {
        final int year = today().getYear();

        assertEquals(year - 1, DateUtil.huntingYearContaining(ld(year, 1, 1)));
        assertEquals(year - 1, DateUtil.huntingYearContaining(ld(year, HUNTING_YEAR_BEGIN_MONTH, 1).minusDays(1)));
        assertEquals(year, DateUtil.huntingYearContaining(ld(year, HUNTING_YEAR_BEGIN_MONTH, 1)));
        assertEquals(year, DateUtil.huntingYearContaining(ld(year, 12, 31)));
    }

    @Test
    public void testHuntingYearInterval() {
        final Interval interval = DateUtil.huntingYearInterval(2014);

        assertEquals(dt(ld(2014, 8, 1), 0, 0, 0), interval.getStart());
        assertEquals(dt(ld(2015, 8, 1), 0, 0, 0), interval.getEnd());
    }

    @Test
    public void testHuntingYearBeginDate() {
        assertEquals(ld(2014, 8, 1), DateUtil.huntingYearBeginDate(2014));
        assertEquals(ld(2015, 8, 1), DateUtil.huntingYearBeginDate(2015));
    }

    @Test
    public void testHuntingYearEndDate() {
        assertEquals(ld(2015, 7, 31), DateUtil.huntingYearEndDate(2014));
        assertEquals(ld(2016, 7, 31), DateUtil.huntingYearEndDate(2015));
    }

    @Test
    public void testOverlapsInclusive_forOneDate() {
        final LocalDate begin = ld(2014, 10, 20);
        final LocalDate end = begin.plusWeeks(1);

        assertTrue(DateUtil.overlapsInclusive(begin, end, begin));
        assertTrue(DateUtil.overlapsInclusive(begin, end, end));
        assertTrue(DateUtil.overlapsInclusive(begin, end, begin.plusDays(1)));
        assertTrue(DateUtil.overlapsInclusive(begin, end, end.minusDays(1)));

        assertFalse(DateUtil.overlapsInclusive(begin, end, end.plusDays(1)));
        assertFalse(DateUtil.overlapsInclusive(begin, end, begin.minusDays(1)));

        assertTrue(DateUtil.overlapsInclusive(null, end, begin));
        assertTrue(DateUtil.overlapsInclusive(null, end, end));
        assertTrue(DateUtil.overlapsInclusive(begin, null, begin));
        assertTrue(DateUtil.overlapsInclusive(begin, null, end));

        assertFalse(DateUtil.overlapsInclusive(begin, null, begin.minusDays(1)));
        assertFalse(DateUtil.overlapsInclusive(null, end, end.plusDays(1)));

        final Date nullDate = null;
        assertTrue(DateUtil.overlapsInclusive(nullDate, nullDate, end));
    }

    @Test
    public void testOverlapsInclusive_forDateRange() {
        final LocalDate begin = ld(2014, 10, 20);
        final LocalDate end = begin.plusDays(2);

        // Pure null cases
        assertTrue(DateUtil.overlapsInclusive(null, null, null, null));
        assertTrue(DateUtil.overlapsInclusive(null, null, begin, end));
        assertTrue(DateUtil.overlapsInclusive(begin, end, null, null));

        assertCommutativeInclusiveOverlapForDateRange(false, begin, end, null, begin.minusDays(1));

        assertCommutativeInclusiveOverlapForDateRange(false, begin, end, begin.minusDays(1), begin.minusDays(1));
        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, begin.minusDays(1), begin);
        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, begin.minusDays(1), end.minusDays(1));
        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, begin.minusDays(1), end);
        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, begin.minusDays(1), end.plusDays(1));
        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, begin.minusDays(1), null);

        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, begin, begin);
        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, begin, end.minusDays(1));
        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, begin, end);
        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, begin, end.plusDays(1));
        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, begin, null);

        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, begin.plusDays(1), end.minusDays(1));
        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, begin.plusDays(1), end);
        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, begin.plusDays(1), end.plusDays(1));
        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, begin.plusDays(1), null);

        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, end, end);
        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, end, end.plusDays(1));
        assertCommutativeInclusiveOverlapForDateRange(true, begin, end, end, null);

        assertCommutativeInclusiveOverlapForDateRange(false, begin, end, end.plusDays(1), end.plusDays(1));
        assertCommutativeInclusiveOverlapForDateRange(false, begin, end, end.plusDays(1), null);
    }

    private static void assertCommutativeInclusiveOverlapForDateRange(final boolean expected,
                                                                      @Nullable final LocalDate begin,
                                                                      @Nullable final LocalDate end,
                                                                      @Nullable final LocalDate begin2,
                                                                      @Nullable final LocalDate end2) {

        assertEquals(expected, DateUtil.overlapsInclusive(begin, end, begin2, end2));
        assertEquals(expected, DateUtil.overlapsInclusive(begin2, end2, begin, end));
    }

    @Test
    public void testParseDateInterval() {
        assertEquals(
                Tuple.of(ld(2014, 8, 20), ld(2014, 10, 31)),
                DateUtil.parseDateInterval("20.8.2014-31.10.2014", DateTimeFormat.forPattern("dd.MM.yyyy")));
    }

    @Test
    public void testParseBlankDateInterval() {
        assertNull(DateUtil.parseDateInterval("", DateTimeFormat.forPattern("dd.MM.yyyy")));
    }

    @Test
    public void testParseDateIntervalWithSpaces() {
        assertEquals(
                Tuple.of(ld(2014, 8, 20), ld(2014, 10, 31)),
                DateUtil.parseDateInterval(" 20.08.2014 - 31.10.2014 ", DateTimeFormat.forPattern("dd.MM.yyyy")));
    }

    @Test
    public void testToDateInterval() {
        final DateTime midDay = now().withHourOfDay(12);
        final LocalDate today = midDay.toLocalDate();

        assertEquals(
                i(today.toDateTimeAtStartOfDay(), today.plusDays(1).toDateTimeAtStartOfDay()),
                DateUtil.toDateInterval(midDay));
    }

    @Test
    public void testHuntingYearsBetween() {
        assertEquals(Arrays.asList(2015), getYearsBetween(2015, 2015));
        assertEquals(Arrays.asList(2015, 2016), getYearsBetween(2015, 2016));
        assertEquals(Arrays.asList(2014, 2015, 2016), getYearsBetween(2014, 2016));
    }

    private static List<Integer> getYearsBetween(final int begin, final int end) {
        return DateUtil.huntingYearsBetween(DateUtil.huntingYearBeginDate(begin), DateUtil.huntingYearEndDate(end))
                .boxed()
                .collect(toList());
    }

    @Test
    public void testCopyDateForHuntingYear() {
        final LocalDate today = today();
        final int currentYear = today.getYear();

        IntStream.of(currentYear + 1, currentYear, currentYear - 1).forEach(year -> {
            final LocalDate date1 = ld(year, 1, 1);
            final LocalDate date2 = DateUtil.huntingYearEndDate(year);
            final LocalDate date3 = DateUtil.huntingYearBeginDate(year);
            final LocalDate date4 = ld(year, 12, 31);

            assertEquals(date1.withYear(year + 1), DateUtil.copyDateForHuntingYear(date1, year));
            assertEquals(date2.withYear(year + 1), DateUtil.copyDateForHuntingYear(date2, year));
            assertEquals(date3.withYear(year), DateUtil.copyDateForHuntingYear(date3, year));
            assertEquals(date4.withYear(year), DateUtil.copyDateForHuntingYear(date4, year));
        });
    }

    @Test
    public void testBeginOfCalendarYear() {
        assertEquals(new DateTime(2017, 1, 1, 0, 0, 0, 0, Constants.DEFAULT_TIMEZONE), DateUtil.beginOfCalendarYear(2017));
    }

    @Test
    public void testMonthAsRange() {
        final Range<DateTime> tuple = DateUtil.monthAsRange(2018, 2);
        assertEquals(1, tuple.lowerEndpoint().getDayOfMonth());
        assertEquals(2, tuple.lowerEndpoint().getMonthOfYear());
        assertEquals(0, tuple.lowerEndpoint().getHourOfDay());
        assertEquals(0, tuple.lowerEndpoint().getMinuteOfHour());

        assertEquals(1, tuple.upperEndpoint().getDayOfMonth());
        assertEquals(3, tuple.upperEndpoint().getMonthOfYear());
        assertEquals(0, tuple.upperEndpoint().getHourOfDay());
        assertEquals(0, tuple.upperEndpoint().getMinuteOfHour());
    }

}
