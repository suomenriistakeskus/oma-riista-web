package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.integration.luke_import.model.v1_0.MooseDataCardGameSpeciesAppearance;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_4;
import io.vavr.control.Validation;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newSection84;
import static fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardSection84Validator.validate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MooseDataCardSection84ValidatorTest {

    @Test
    public void testValidate_withValidData() {
        final MooseDataCardSection_8_4 input = newSection84();
        final Validation<List<String>, MooseDataCardSection_8_4> validation = validate(input);
        assertTrue(validation.isValid());

        validation.peek(output -> {
            assertEquals(input.getMooseHeatBeginDate(), output.getMooseHeatBeginDate());
            assertEquals(input.getMooseHeatEndDate(), output.getMooseHeatEndDate());
            assertEquals(input.getMooseFawnBeginDate(), output.getMooseFawnBeginDate());
            assertEquals(input.getMooseFawnEndDate(), output.getMooseFawnEndDate());

            assertEquals(input.getDeerFlyAppearead(), output.getDeerFlyAppearead());
            assertEquals(input.getDateOfFirstDeerFlySeen(), output.getDateOfFirstDeerFlySeen());
            assertEquals(input.getDateOfLastDeerFlySeen(), output.getDateOfLastDeerFlySeen());
            assertEquals(input.getNumberOfAdultMoosesHavingFlies(), output.getNumberOfAdultMoosesHavingFlies());
            assertEquals(input.getNumberOfYoungMoosesHavingFlies(), output.getNumberOfYoungMoosesHavingFlies());
            assertEquals(input.getTrendOfDeerFlyPopulationGrowth(), output.getTrendOfDeerFlyPopulationGrowth());
        });
    }

    @Test
    public void testValidate_withEmptyData() {
        final Validation<List<String>, MooseDataCardSection_8_4> validation = validate(new MooseDataCardSection_8_4());
        assertTrue(validation.isValid());

        validation.peek(output -> {
            assertNull(output.getMooseHeatBeginDate());
            assertNull(output.getMooseHeatEndDate());
            assertNull(output.getMooseFawnBeginDate());
            assertNull(output.getMooseFawnEndDate());

            assertNull(output.getDeerFlyAppearead());
            assertNull(output.getDateOfFirstDeerFlySeen());
            assertNull(output.getDateOfLastDeerFlySeen());
            assertNull(output.getNumberOfAdultMoosesHavingFlies());
            assertNull(output.getNumberOfYoungMoosesHavingFlies());
            assertNull(output.getTrendOfDeerFlyPopulationGrowth());
        });
    }

    @Test
    public void testValidate_withOutOfRangeNumericValues() {
        final MooseDataCardSection_8_4 input = new MooseDataCardSection_8_4()
                .withNumberOfAdultMoosesHavingFlies(-1)
                .withNumberOfYoungMoosesHavingFlies(-1);

        final Validation<List<String>, MooseDataCardSection_8_4> validation = validate(input);
        assertTrue(validation.isValid());

        validation.peek(output -> {
            assertNull(output.getNumberOfAdultMoosesHavingFlies());
            assertNull(output.getNumberOfYoungMoosesHavingFlies());
        });
    }

    @Test
    public void testValidate_withIllegalDateRanges() {
        final MooseDataCardSection_8_4 input = newSection84();
        input.setMooseHeatEndDate(input.getMooseHeatBeginDate().minusDays(1));
        input.setMooseFawnEndDate(input.getMooseFawnBeginDate().minusDays(1));
        input.setDateOfLastDeerFlySeen(input.getDateOfFirstDeerFlySeen().minusDays(1));

        final Validation<List<String>, MooseDataCardSection_8_4> validation = validate(input);
        assertTrue(validation.isValid());

        validation.peek(output -> {
            assertEquals(input.getMooseHeatBeginDate(), output.getMooseHeatBeginDate());
            assertNull(output.getMooseHeatEndDate());
            assertEquals(input.getMooseFawnBeginDate(), output.getMooseFawnBeginDate());
            assertNull(output.getMooseFawnEndDate());
            assertEquals(input.getDateOfFirstDeerFlySeen(), output.getDateOfFirstDeerFlySeen());
            assertNull(output.getDateOfLastDeerFlySeen());
        });
    }

    @Test
    public void testValidate_whenDeerFlyNotAppeared() {
        Stream.of(MooseDataCardGameSpeciesAppearance.NO).forEach(appearance -> {
            final MooseDataCardSection_8_4 input = newSection84().withDeerFlyAppearead(appearance);

            final Validation<List<String>, MooseDataCardSection_8_4> validation = validate(input);
            assertTrue(validation.isValid());

            validation.peek(output -> {
                assertNull(output.getDateOfFirstDeerFlySeen());
                assertNull(output.getDateOfLastDeerFlySeen());
                assertNull(output.getNumberOfAdultMoosesHavingFlies());
                assertNull(output.getNumberOfYoungMoosesHavingFlies());
                assertNull(output.getTrendOfDeerFlyPopulationGrowth());
            });
        });
    }
}
