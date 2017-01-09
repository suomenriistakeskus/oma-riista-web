package fi.riista.feature.huntingclub.hunting.day;

import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GroupHuntingDayEntityTest {

    @Test
    public void testHuntingDayDayDurationAllFieldsNull() {
        assertEquals(0, createDay(null, null, null, null, null).calculateHuntingDayDurationInMinutes());
    }

    @Test
    public void testHuntingDayDurationBreaksIsNull() {
        assertEquals(98, createDay(today(), time(), today(), time(98), null).calculateHuntingDayDurationInMinutes());
    }

    @Test
    public void testHuntingDayDuration() {
        assertEquals(87, createDay(today(), time(), today(), time(98), 11).calculateHuntingDayDurationInMinutes());
    }

    private static LocalTime time() {
        return time(null);
    }

    private static LocalTime time(Integer deltaMinutes) {
        LocalTime t = new LocalTime(12, 0, 0);
        return deltaMinutes != null ? t.plusMinutes(deltaMinutes) : t;
    }

    private static LocalDate today() {
        return DateUtil.today();
    }

    private static GroupHuntingDay createDay(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime, Integer breaks) {
        GroupHuntingDay day = new GroupHuntingDay();
        day.setStartDate(startDate);
        day.setStartTime(startTime);
        day.setEndDate(endDate);
        day.setEndTime(endTime);
        day.setBreakDurationInMinutes(breaks);
        return day;
    }
}
