package fi.riista.feature.huntingclub.moosedatacard;

import fi.riista.config.Constants;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardHuntingDayField;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Optional;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.getHuntingDayInterval;
import static fi.riista.test.TestUtils.dt;
import static fi.riista.test.TestUtils.i;
import static fi.riista.test.TestUtils.ld;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class MooseDataCardExtractor_HuntingDayIntervalTest {

    @Test
    public void testGetHuntingDayInterval_whenDurationBelowMinLimit() {
        final Optional<Float> minDuration = MooseDataCardHuntingDayField.HUNTING_DAY_DURATION.findLowerBound();
        assumeTrue("Minimum duration of hunting day not defined", minDuration.isPresent());

        final LocalDate today = today();

        final DateTime expectedStartTime = today.toDateTime(MooseDataCardExtractor.DEFAULT_HUNTING_DAY_START_TIME)
                .withZone(Constants.DEFAULT_TIMEZONE);

        assertEquals(
                i(expectedStartTime, expectedStartTime.plusHours(MooseDataCardExtractor.DEFAULT_DURATION)),
                getHuntingDayInterval(today, minDuration.get().floatValue() - 0.5f));
    }

    @Test
    public void testGetHuntingDayInterval_whenDurationAboveMaxLimit() {
        final Optional<Float> maxDuration = MooseDataCardHuntingDayField.HUNTING_DAY_DURATION.findUpperBound();
        assumeTrue("Maximum duration of hunting day not defined", maxDuration.isPresent());

        final LocalDate today = today();
        final Interval resultInterval = getHuntingDayInterval(today, maxDuration.get() + 0.5f);

        assertEquals(Duration.standardHours(MooseDataCardExtractor.DEFAULT_DURATION), resultInterval.toDuration());

        assertEquals(today, resultInterval.getStart().toLocalDate());
    }

    @Test
    public void testGetHuntingDayInterval() {
        final LocalDate today = ld(2016, 10, 1);

        assertEquals(i(dt(today, 8), dt(today, 14)), getHuntingDayInterval(today, 6f));
        assertEquals(i(dt(today, 8), dt(today.plusDays(1), 3)), getHuntingDayInterval(today, 19f));
        assertEquals(i(dt(today, 8), dt(today.plusDays(2), 0)), getHuntingDayInterval(today, 40f));
        assertEquals(i(dt(today, 3), dt(today.plusDays(2), 0)), getHuntingDayInterval(today, 45f));
        assertEquals(i(dt(today, 0, 30), dt(today.plusDays(2), 0)), getHuntingDayInterval(today, 47.5f));
    }
}
