package fi.riista.feature.organization.rhy;

import org.joda.time.LocalTime;
import org.junit.Test;

import static fi.riista.util.DateUtil.today;

public class RhyEventTimeExceptionTest {

    @Test(expected = RhyEventTimeException.class)
    public void testEventTimeTooFarInThePast() {
        RhyEventTimeException.assertEventNotTooFarInPast(today().minusYears(2), false);
    }

    @Test
    public void testEventTimeTooFarInThePastDoesNotThrowExceptionForModerator() {
        RhyEventTimeException.assertEventNotTooFarInPast(today().minusYears(2), true);
    }

    @Test(expected = RhyEventTimeException.class)
    public void testEventLastModificationTime() {
        RhyEventTimeException.assertEventLastModificationTime(today().minusDays(1), false);
    }

    @Test
    public void testEventLastModificationTimeDoesNotThrowExceptionForModerator() {
        RhyEventTimeException.assertEventLastModificationTime(today().minusDays(1), true);
    }

    @Test(expected = RhyEventTimeException.class)
    public void testEventTimeInTheFuture() {
        RhyEventTimeException.assertEventNotInFuture(today().plusDays(1));
    }

    @Test(expected = RhyEventTimeException.class)
    public void testEventBeginTimeAfterEndTime() {
        RhyEventTimeException.assertBeginTimeNotAfterEndTime(new LocalTime(12,0), new LocalTime(11, 0));
    }
}
