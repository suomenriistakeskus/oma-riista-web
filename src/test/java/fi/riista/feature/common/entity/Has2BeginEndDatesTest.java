package fi.riista.feature.common.entity;

import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.OptionalInt;

import static fi.riista.test.TestUtils.ld;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;

public class Has2BeginEndDatesTest {

    private static final LocalDate DATE = today();

    @Test
    public void testContainsDate_whenFirstDatesDefined() {
        final Has2BeginEndDates object = new Has2BeginEndDatesDTO(DATE, DATE);

        testContainsDate(object, DATE, true);
        testContainsDate(object, DATE.minusDays(1), false);
        testContainsDate(object, DATE.plusDays(1), false);
    }

    @Test
    public void testContainsDate_whenSecondDatesDefined() {
        final Has2BeginEndDates object = new Has2BeginEndDatesDTO(null, null, DATE, DATE);

        testContainsDate(object, DATE, true);
        testContainsDate(object, DATE.minusDays(1), false);
        testContainsDate(object, DATE.plusDays(1), false);
    }

    @Test
    public void testContainsDate_whenAllDatesDefined() {
        final Has2BeginEndDates object =
                new Has2BeginEndDatesDTO(DATE.minusDays(2), DATE.minusDays(1), DATE.plusDays(1), DATE.plusDays(2));

        testContainsDate(object, DATE.minusDays(2), true);
        testContainsDate(object, DATE.minusDays(1), true);
        testContainsDate(object, DATE.plusDays(1), true);
        testContainsDate(object, DATE.plusDays(2), true);

        testContainsDate(object, DATE.minusDays(3), false);
        testContainsDate(object, DATE, false);
        testContainsDate(object, DATE.plusDays(3), false);
    }

    @Test
    public void testContainsDate_whenAllDatesNull() {
        testContainsDate(new Has2BeginEndDatesDTO(), DATE, false);
    }

    private static void testContainsDate(final Has2BeginEndDates dates, final LocalDate date, final boolean expected) {
        assertEquals(expected, dates.containsDate(date));
    }

    @Test
    public void testFindUnambiguousHuntingYear() {
        final LocalDate DATE1 = ld(2015, 8, 1);
        final LocalDate DATE2 = ld(2015, 12, 31);
        final LocalDate DATE3 = ld(2016, 2, 1);
        final LocalDate DATE4 = ld(2016, 7, 31);

        final OptionalInt empty = OptionalInt.empty();

        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(DATE1, null), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(null, DATE1), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(null, null, DATE1, null), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(null, null, null, DATE1), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(DATE1, null, DATE2, null), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(DATE1, null, null, DATE2), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(null, DATE1, DATE2, null), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(null, DATE1, null, DATE2), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(DATE1, DATE2, DATE3, null), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(DATE1, DATE2, null, DATE3), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(null, DATE1, DATE2, DATE3), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(DATE1, null, DATE2, DATE3), empty);

        final LocalDate prevYearDate = DATE1.minusDays(1);
        final LocalDate nextYearDate = DATE4.plusDays(1);

        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(prevYearDate, DATE2), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(prevYearDate, DATE2, DATE3, DATE4), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(null, null, prevYearDate, DATE2), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(DATE1, nextYearDate), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(null, null, DATE3, nextYearDate), empty);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(DATE1, DATE2, DATE3, nextYearDate), empty);

        final OptionalInt expected = OptionalInt.of(2015);

        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(DATE1, DATE1), expected);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(DATE1, DATE2), expected);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(DATE1, DATE4), expected);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(DATE3, DATE4), expected);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(DATE4, DATE4), expected);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(null, null, DATE1, DATE1), expected);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(null, null, DATE1, DATE2), expected);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(null, null, DATE1, DATE4), expected);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(null, null, DATE3, DATE4), expected);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(null, null, DATE4, DATE4), expected);
        testFindUnambiguousHuntingYear(new Has2BeginEndDatesDTO(DATE1, DATE2, DATE3, DATE4), expected);
    }

    private static void testFindUnambiguousHuntingYear(final Has2BeginEndDates dates, final OptionalInt expected) {
        assertEquals(expected, dates.findUnambiguousHuntingYear());
    }
}
