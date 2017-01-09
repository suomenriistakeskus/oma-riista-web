package fi.riista.feature.huntingclub.moosedatacard.converter;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newHuntingDay;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardHuntingDay;

import org.junit.Test;

public class MooseDataCardHuntingDayConverterTest {

    @Test
    public void testWithCompleteValidData() {
        final MooseDataCardHuntingDay input = newHuntingDay(today());

        final GroupHuntingDay output = new MooseDataCardHuntingDayConverter().convert(input);
        assertEquals(input.getStartDate(), output.getStartDate());
        assertEquals(input.getStartDate(), output.getEndDate());
        assertEquals(MooseDataCardExtractor.DEFAULT_HUNTING_DAY_START_TIME, output.getStartTime());
        assertEquals(
                MooseDataCardExtractor.DEFAULT_HUNTING_DAY_START_TIME
                        .plusMinutes(Float.valueOf(input.getHuntingTime() * 60f).intValue()),
                output.getEndTime());
        assertEquals(input.getSnowDepth(), output.getSnowDepth());
        assertEquals(input.getHuntingMethod().intValue(), output.getHuntingMethod().getTypeCode());
        assertEquals(input.getNumberOfHunters(), output.getNumberOfHunters());
        assertEquals(input.getNumberOfHounds(), output.getNumberOfHounds());
    }

    @Test
    public void testWithInvalidValues() {
        assertionsForInvalidOrMissingValues(newHuntingDay(today())
                .withHuntingTime(null)
                .withHuntingMethod(-1)
                .withSnowDepth(-1)
                .withNumberOfHunters(-1)
                .withNumberOfHounds(-1));
    }

    @Test
    public void testWithMissingValues() {
        assertionsForInvalidOrMissingValues(newHuntingDay(today())
                .withHuntingTime(null)
                .withHuntingMethod(null)
                .withSnowDepth(null)
                .withNumberOfHunters(null)
                .withNumberOfHounds(null));
    }

    private static void assertionsForInvalidOrMissingValues(final MooseDataCardHuntingDay input) {
        final GroupHuntingDay output = new MooseDataCardHuntingDayConverter().convert(input);
        assertEquals(input.getStartDate(), output.getStartDate());
        assertEquals(input.getStartDate(), output.getEndDate());
        assertEquals(MooseDataCardExtractor.DEFAULT_HUNTING_DAY_START_TIME, output.getStartTime());
        assertEquals(
                MooseDataCardExtractor.DEFAULT_HUNTING_DAY_START_TIME
                        .plusHours(MooseDataCardExtractor.DEFAULT_DURATION),
                output.getEndTime());
        assertNull(output.getSnowDepth());
        assertNull(output.getHuntingMethod());
        assertNull(output.getNumberOfHunters());
        assertNull(output.getNumberOfHounds());
    }

}
