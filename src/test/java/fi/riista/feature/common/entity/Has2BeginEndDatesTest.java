package fi.riista.feature.common.entity;

import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.junit.Test;

public class Has2BeginEndDatesTest {

    @Test
    public void testContainsDate_whenFirstDatesDefined() {
        final Has2BeginEndDates object = new Has2BeginEndDatesDTO(today(), today());

        testContainsDate(object, today(), true);
        testContainsDate(object, today().minusDays(1), false);
        testContainsDate(object, today().plusDays(1), false);
    }

    @Test
    public void testContainsDate_whenSecondDatesDefined() {
        final Has2BeginEndDates object = new Has2BeginEndDatesDTO(null, null, today(), today());

        testContainsDate(object, today(), true);
        testContainsDate(object, today().minusDays(1), false);
        testContainsDate(object, today().plusDays(1), false);
    }

    @Test
    public void testContainsDate_whenAllDatesDefined() {
        final Has2BeginEndDates object = new Has2BeginEndDatesDTO(
                today().minusDays(2), today().minusDays(1), today().plusDays(1), today().plusDays(2));

        testContainsDate(object, today().minusDays(2), true);
        testContainsDate(object, today().minusDays(1), true);
        testContainsDate(object, today().plusDays(1), true);
        testContainsDate(object, today().plusDays(2), true);

        testContainsDate(object, today().minusDays(3), false);
        testContainsDate(object, today(), false);
        testContainsDate(object, today().plusDays(3), false);
    }

    @Test
    public void testContainsDate_whenAllDatesNull() {
        testContainsDate(new Has2BeginEndDatesDTO(), today(), false);
    }

    private static void testContainsDate(
            final Has2BeginEndDates dates, final LocalDate date, final boolean expected) {

        assertEquals(expected, dates.containsDate(date));
    }

}
