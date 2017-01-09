package fi.riista.util;

import javaslang.Tuple;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static fi.riista.util.DateUtil.HUNTING_YEAR_BEGIN_MONTH;
import static fi.riista.util.DateUtil.getFirstCalendarYearOfHuntingYearContaining;
import static fi.riista.util.DateUtil.now;
import static fi.riista.util.DateUtil.today;
import static fi.riista.util.TestUtils.dt;
import static fi.riista.util.TestUtils.i;
import static fi.riista.util.TestUtils.ld;
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
    public void testGetFirstCalendarYearOfHuntingYearContaining() {
        final int year = today().getYear();

        assertEquals(year - 1, getFirstCalendarYearOfHuntingYearContaining(ld(year, 1, 1)));
        assertEquals(
                year - 1, getFirstCalendarYearOfHuntingYearContaining(ld(year, HUNTING_YEAR_BEGIN_MONTH, 1).minusDays(1)));
        assertEquals(year, getFirstCalendarYearOfHuntingYearContaining(ld(year, HUNTING_YEAR_BEGIN_MONTH, 1)));
        assertEquals(year, getFirstCalendarYearOfHuntingYearContaining(ld(year, 12, 31)));
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
        assertTrue(DateUtil.overlapsInclusive(begin, null, begin));
        assertTrue(DateUtil.overlapsInclusive(null, end, end));
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

    private static void assertCommutativeInclusiveOverlapForDateRange(
            final boolean expected,
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
        assertNull(DateUtil.parseDateInterval(
                "", DateTimeFormat.forPattern("dd.MM.yyyy")));
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

    private static List<Integer> getYearsBetween(int begin, int end) {
        return DateUtil.huntingYearsBetween(DateUtil.huntingYearBeginDate(begin), DateUtil.huntingYearEndDate(end))
                .boxed()
                .collect(toList());
    }
}
