package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.Has2BeginEndDatesDTO;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardHuntingDay;
import io.vavr.control.Either;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.huntingDayEndDateNotWithinPermittedSeason;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.huntingDayStartDateNotWithinPermittedSeason;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.huntingDayWithoutDate;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newHuntingDay;
import static fi.riista.test.TestUtils.ld;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MooseDataCardHuntingDayValidatorTest {

    private Has2BeginEndDates permitSeason;

    @Before
    public void setup() {
        permitSeason = new Has2BeginEndDatesDTO(ld(2015, 9, 1), ld(2015, 12, 31));
    }

    @Test
    public void testWithCompleteData() {
        final MooseDataCardHuntingDay input = newHuntingDayWithinSeason();

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
        assertAbandonReason(newHuntingDay(null), huntingDayWithoutDate());
    }

    @Test
    public void testCorrectionOfWrongHuntingYear() {
        final LocalDate lastPermitDate = permitSeason.getLastDate();
        final MooseDataCardHuntingDay input = newHuntingDay(lastPermitDate.plusYears(2));

        assertAccepted(input, output -> assertEquals(lastPermitDate, output.getStartDate()));
    }

    @Test
    public void testWhenDateBeforePermittedSeason() {
        final LocalDate huntingDate = permitSeason.getFirstDate().minusDays(1);

        assertAbandonReason(
                newHuntingDay(huntingDate),
                huntingDayStartDateNotWithinPermittedSeason(huntingDate, permitSeason));
    }

    @Test
    public void testWhenDateAfterPermittedSeason() {
        final LocalDate seasonBeginDate = permitSeason.getBeginDate();
        permitSeason.setEndDate(seasonBeginDate);

        final MooseDataCardHuntingDay huntingDay = newHuntingDayWithinSeason();
        huntingDay.setHuntingTime(25.0f);

        assertAbandonReason(
                huntingDay,
                huntingDayEndDateNotWithinPermittedSeason(seasonBeginDate, seasonBeginDate.plusDays(1), permitSeason));
    }

    @Test
    public void testWhenDurationTooLow() {
        final MooseDataCardHuntingDay input = newHuntingDayWithinSeason();
        input.setHuntingTime(0f);

        assertAccepted(input, output -> assertNull(output.getHuntingTime()));
    }

    @Test
    public void testWhenDurationTooHigh() {
        final MooseDataCardHuntingDay input = newHuntingDayWithinSeason();
        input.setHuntingTime(Float.MAX_VALUE);

        assertAccepted(input, output -> assertNull(output.getHuntingTime()));
    }

    @Test
    public void testWhenSnowDepthTooLow() {
        final MooseDataCardHuntingDay input = newHuntingDayWithinSeason();
        input.setSnowDepth(-1);

        assertAccepted(input, output -> assertNull(output.getSnowDepth()));
    }

    @Test
    public void testWhenSnowDepthTooHigh() {
        final MooseDataCardHuntingDay input = newHuntingDayWithinSeason();
        input.setSnowDepth(Integer.MAX_VALUE);

        assertAccepted(input, output -> assertNull(output.getSnowDepth()));
    }

    @Test
    public void testWhenHuntingDayMethodInvalid() {
        final MooseDataCardHuntingDay input = newHuntingDayWithinSeason();
        input.setHuntingMethod(-1);

        assertAccepted(input, output -> assertNull(output.getHuntingMethod()));
    }

    @Test
    public void testWhenNumberOfHuntersTooLow() {
        final MooseDataCardHuntingDay input = newHuntingDayWithinSeason();
        input.setNumberOfHunters(0);

        assertAccepted(input, output -> assertNull(output.getNumberOfHunters()));
    }

    @Test
    public void testWhenNumberOfHuntersTooHigh() {
        final MooseDataCardHuntingDay input = newHuntingDayWithinSeason();
        input.setNumberOfHunters(Integer.MAX_VALUE);

        assertAccepted(input, output -> assertNull(output.getNumberOfHunters()));
    }

    @Test
    public void testWhenNumberOfHoundsTooLow() {
        final MooseDataCardHuntingDay input = newHuntingDayWithinSeason();
        input.setNumberOfHounds(-1);

        assertAccepted(input, output -> assertNull(output.getNumberOfHounds()));
    }

    @Test
    public void testWhenNumberOfHoundsTooHigh() {
        final MooseDataCardHuntingDay input = newHuntingDayWithinSeason();
        input.setNumberOfHounds(Integer.MAX_VALUE);

        assertAccepted(input, output -> assertNull(output.getNumberOfHounds()));
    }

    private void assertAccepted(final MooseDataCardHuntingDay day, final Consumer<MooseDataCardHuntingDay> assertions) {
        final Either<String, MooseDataCardHuntingDay> result = validate(day);
        assertTrue(result.isRight());
        assertions.accept(result.get());
    }

    private void assertAbandonReason(final MooseDataCardHuntingDay day, final String expectedReason) {
        final Either<String, MooseDataCardHuntingDay> result = validate(day);
        assertTrue(result.isLeft());
        assertEquals(expectedReason, result.getLeft());
    }

    private Either<String, MooseDataCardHuntingDay> validate(final MooseDataCardHuntingDay day) {
        return new MooseDataCardHuntingDayValidator(permitSeason).validate(day);
    }

    private MooseDataCardHuntingDay newHuntingDayWithinSeason() {
        return newHuntingDay(permitSeason.getFirstDate());
    }
}
