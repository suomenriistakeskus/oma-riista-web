package fi.riista.feature.huntingclub.hunting.day;

import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GroupHuntingDayEntityTest {

    @Test
    public void testHuntingDayDayDurationAllFieldsNull() {
        assertEquals(0, createDay(null, null, null, null, null).calculateHuntingDayDurationInMinutes());
    }

    @Test
    public void testHuntingDayDurationBreaksIsNull() {
        final LocalDate today = today();
        assertEquals(98, createDay(today, time(), today, time(98), null).calculateHuntingDayDurationInMinutes());
    }

    @Test
    public void testHuntingDayDuration() {
        final LocalDate today = today();
        assertEquals(87, createDay(today, time(), today, time(98), 11).calculateHuntingDayDurationInMinutes());
    }

    @Test
    public void testContainsInstant() {
        final LocalDate today = today();
        final DateTime start = today.toDateTime(time());
        final DateTime end = today.plusDays(1).toDateTime(time());

        final GroupHuntingDay day =
                createDay(start.toLocalDate(), start.toLocalTime(), end.toLocalDate(), end.toLocalTime(), null);

        assertTrue(day.containsInstant(start));
        assertTrue(day.containsInstant(end));
        assertTrue(day.containsInstant(start.plusSeconds(1)));
        assertTrue(day.containsInstant(end.minusSeconds(1)));
        assertFalse(day.containsInstant(start.minusSeconds(1)));
        assertFalse(day.containsInstant(end.plusSeconds(1)));
    }

    @Test
    public void testCreateAllDayHuntingDayForGroup() {
        final LocalDate today = today();
        final HuntingClubGroup group = new HuntingClubGroup();

        final GroupHuntingDay day = GroupHuntingDay.createAllDayHuntingDayForGroup(today, group);

        assertEquals(today, day.getStartDate());
        assertEquals(new LocalTime(0, 0), day.getStartTime());
        assertEquals(today, day.getEndDate());
        assertEquals(new LocalTime(23, 59), day.getEndTime());
        assertEquals(group, day.getGroup());
    }

    private static LocalTime time() {
        return time(null);
    }

    private static LocalTime time(final Integer deltaMinutes) {
        final LocalTime t = new LocalTime(12, 0);
        return deltaMinutes != null ? t.plusMinutes(deltaMinutes) : t;
    }

    private static GroupHuntingDay createDay(final LocalDate startDate,
                                             final LocalTime startTime,
                                             final LocalDate endDate,
                                             final LocalTime endTime,
                                             final Integer breaks) {

        final GroupHuntingDay day = new GroupHuntingDay();
        day.setStartDate(startDate);
        day.setStartTime(startTime);
        day.setEndDate(endDate);
        day.setEndTime(endTime);
        day.setBreakDurationInMinutes(breaks);
        return day;
    }
}
