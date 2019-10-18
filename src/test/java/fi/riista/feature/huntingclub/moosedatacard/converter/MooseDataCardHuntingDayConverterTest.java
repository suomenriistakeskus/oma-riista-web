package fi.riista.feature.huntingclub.moosedatacard.converter;

import fi.riista.feature.common.dto.Has2BeginEndDatesDTO;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardHuntingDay;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newHuntingDay;
import static fi.riista.test.TestUtils.ld;
import static fi.riista.util.DateUtil.copyDateForHuntingYear;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MooseDataCardHuntingDayConverterTest {

    private Has2BeginEndDates season;
    private MooseDataCardHuntingDay input;

    @Before
    public void setup() {
        season = new Has2BeginEndDatesDTO(ld(2015, 9, 1), ld(2016, 1, 31));
        input = newHuntingDay(season.getFirstDate());
    }

    @Test
    public void testWithCompleteValidData() {
        final GroupHuntingDay output = convert(season, input);

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
    public void testCorrectionOfWrongHuntingYear() {
        final LocalDate lastPermitDate = season.getLastDate();
        input.setStartDate(lastPermitDate.minusYears(2));

        final GroupHuntingDay output = convert(season, input);
        assertEquals(copyDateForHuntingYear(input.getStartDate(), season.resolveHuntingYear()), output.getStartDate());
    }

    @Test
    public void testWithInvalidValues() {
        assertionsForInvalidOrMissingValues(season, input.withHuntingTime(null)
                .withHuntingMethod(-1)
                .withSnowDepth(-1)
                .withNumberOfHunters(-1)
                .withNumberOfHounds(-1));
    }

    @Test
    public void testWithMissingValues() {
        assertionsForInvalidOrMissingValues(season, input.withHuntingTime(null)
                .withHuntingMethod(null)
                .withSnowDepth(null)
                .withNumberOfHunters(null)
                .withNumberOfHounds(null));
    }

    private static GroupHuntingDay convert(final Has2BeginEndDates season, final MooseDataCardHuntingDay input) {
        return new MooseDataCardHuntingDayConverter(season).convert(input);
    }

    private static void assertionsForInvalidOrMissingValues(final Has2BeginEndDates season,
                                                            final MooseDataCardHuntingDay input) {

        final GroupHuntingDay output = convert(season, input);

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
