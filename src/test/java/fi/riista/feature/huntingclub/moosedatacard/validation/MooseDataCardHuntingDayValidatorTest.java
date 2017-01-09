package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.huntingDayEndDateNotWithinPermittedSeason;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.huntingDayStartDateNotWithinPermittedSeason;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.huntingDayWithoutDate;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newHuntingDay;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.Has2BeginEndDatesDTO;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardHuntingDay;

import javaslang.control.Either;

import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.function.Consumer;

public class MooseDataCardHuntingDayValidatorTest {

    @Test
    public void testWithCompleteData() {
        final MooseDataCardHuntingDay input = newHuntingDay(today());

        assertAccepted(input, output -> {
            assertEquals(input.getStartDate(), output.getStartDate());
            assertEquals(input.getHuntingTime(), output.getHuntingTime());
            assertEquals(input.getHuntingMethod(), output.getHuntingMethod());
            assertEquals(input.getNumberOfHunters(), output.getNumberOfHunters());
            assertEquals(input.getNumberOfHounds(), output.getNumberOfHounds());
        });
    }

    @Test
    public void testMissingDate() {
        final LocalDate today = today();
        assertAbandonReason(newHuntingDay(null), newSeason(today, today), huntingDayWithoutDate());
    }

    @Test
    public void testWhenDateBeforePermittedSeason() {
        final LocalDate today = today();
        final LocalDate huntingDate = today.minusDays(1);
        final Has2BeginEndDates season = newSeason(today, today);
        assertAbandonReason(
                newHuntingDay(huntingDate), season, huntingDayStartDateNotWithinPermittedSeason(huntingDate, season));
    }

    @Test
    public void testWhenDateAfterPermittedSeason() {
        final LocalDate today = today();

        final MooseDataCardHuntingDay huntingDay = newHuntingDay(today);
        huntingDay.setHuntingTime(25.0f);

        final Has2BeginEndDates season = newSeason(today, today);

        assertAbandonReason(
                huntingDay, season, huntingDayEndDateNotWithinPermittedSeason(today, today.plusDays(1), season));
    }

    @Test
    public void testWhenDurationTooLow() {
        final MooseDataCardHuntingDay input = newHuntingDay(today());
        input.setHuntingTime(0f);
        assertAccepted(input, output -> assertNull(output.getHuntingTime()));
    }

    @Test
    public void testWhenDurationTooHigh() {
        final MooseDataCardHuntingDay input = newHuntingDay(today());
        input.setHuntingTime(Float.MAX_VALUE);
        assertAccepted(input, output -> assertNull(output.getHuntingTime()));
    }

    @Test
    public void testWhenSnowDepthTooLow() {
        final MooseDataCardHuntingDay input = newHuntingDay(today());
        input.setSnowDepth(-1);
        assertAccepted(input, output -> assertNull(output.getSnowDepth()));
    }

    @Test
    public void testWhenSnowDepthTooHigh() {
        final MooseDataCardHuntingDay input = newHuntingDay(today());
        input.setSnowDepth(Integer.MAX_VALUE);
        assertAccepted(input, output -> assertNull(output.getSnowDepth()));
    }

    @Test
    public void testWhenHuntingDayMethodInvalid() {
        final MooseDataCardHuntingDay input = newHuntingDay(today());
        input.setHuntingMethod(-1);
        assertAccepted(input, output -> assertNull(output.getHuntingMethod()));
    }

    @Test
    public void testWhenNumberOfHuntersTooLow() {
        final MooseDataCardHuntingDay input = newHuntingDay(today());
        input.setNumberOfHunters(0);
        assertAccepted(input, output -> assertNull(output.getNumberOfHunters()));
    }

    @Test
    public void testWhenNumberOfHuntersTooHigh() {
        final MooseDataCardHuntingDay input = newHuntingDay(today());
        input.setNumberOfHunters(Integer.MAX_VALUE);
        assertAccepted(input, output -> assertNull(output.getNumberOfHunters()));
    }

    @Test
    public void testWhenNumberOfHoundsTooLow() {
        final MooseDataCardHuntingDay input = newHuntingDay(today());
        input.setNumberOfHounds(-1);
        assertAccepted(input, output -> assertNull(output.getNumberOfHounds()));
    }

    @Test
    public void testWhenNumberOfHoundsTooHigh() {
        final MooseDataCardHuntingDay input = newHuntingDay(today());
        input.setNumberOfHounds(Integer.MAX_VALUE);
        assertAccepted(input, output -> assertNull(output.getNumberOfHounds()));
    }

    private static void assertAccepted(
            final MooseDataCardHuntingDay day, final Consumer<MooseDataCardHuntingDay> assertions) {

        final LocalDate date = day.getStartDate();

        final Either<String, MooseDataCardHuntingDay> result =
                new MooseDataCardHuntingDayValidator(newSeason(date, date)).validate(day);
        assertTrue(result.isRight());

        assertions.accept(result.get());
    }

    private static void assertAbandonReason(
            final MooseDataCardHuntingDay day, final Has2BeginEndDates season, final String expectedReason) {

        final Either<String, MooseDataCardHuntingDay> result =
                new MooseDataCardHuntingDayValidator(season).validate(day);
        assertTrue(result.isLeft());
        assertEquals(expectedReason, result.getLeft());
    }

    private static Has2BeginEndDates newSeason(final LocalDate startDate, final LocalDate endDate) {
        return new Has2BeginEndDatesDTO(startDate, endDate, null, null);
    }

}
