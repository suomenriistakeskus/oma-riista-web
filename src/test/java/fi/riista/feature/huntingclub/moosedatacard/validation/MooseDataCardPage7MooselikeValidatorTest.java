package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newPage7;
import static fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardPage7MooselikeValidator.validate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.riista.integration.luke_import.model.v1_0.MooseDataCardGameSpeciesAppearance;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage7;

import javaslang.control.Validation;

import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

public class MooseDataCardPage7MooselikeValidatorTest {

    @Test
    public void testValidate_withCompleteValidData() {
        final MooseDataCardPage7 input = newPage7();
        final Validation<List<String>, MooseDataCardPage7> validation = validate(input);
        assertTrue(validation.isValid());

        validation.peek(output -> {
            assertEquals(input.getWhiteTailedDeerAppeared(), output.getWhiteTailedDeerAppeared());
            assertEquals(input.getRoeDeerAppeared(), output.getRoeDeerAppeared());
            assertEquals(input.getWildForestReindeerAppeared(), output.getWildForestReindeerAppeared());
            assertEquals(input.getFallowDeerAppeared(), output.getFallowDeerAppeared());

            assertEquals(
                    input.getEstimatedSpecimenAmountOfWhiteTailedDeer(),
                    output.getEstimatedSpecimenAmountOfWhiteTailedDeer());
            assertEquals(input.getEstimatedSpecimenAmountOfRoeDeer(), output.getEstimatedSpecimenAmountOfRoeDeer());
            assertEquals(
                    input.getEstimatedSpecimenAmountOfWildForestReindeer(),
                    output.getEstimatedSpecimenAmountOfWildForestReindeer());
            assertEquals(
                    input.getEstimatedSpecimenAmountOfFallowDeer(), output.getEstimatedSpecimenAmountOfFallowDeer());

            assertEquals(
                    input.getTrendOfWhiteTailedDeerPopulationGrowth(),
                    output.getTrendOfWhiteTailedDeerPopulationGrowth());
            assertEquals(input.getTrendOfRoeDeerPopulationGrowth(), output.getTrendOfRoeDeerPopulationGrowth());
            assertEquals(
                    input.getTrendOfWildForestReindeerPopulationGrowth(),
                    output.getTrendOfWildForestReindeerPopulationGrowth());
            assertEquals(input.getFallowDeerAppeared(), output.getFallowDeerAppeared());
        });
    }

    @Test
    public void testValidate_withEmptyData() {
        final MooseDataCardPage7 input = new MooseDataCardPage7();
        final Validation<List<String>, MooseDataCardPage7> validation = validate(input);
        assertTrue(validation.isValid());

        validation.peek(output -> {
            assertNull(output.getWhiteTailedDeerAppeared());
            assertNull(output.getRoeDeerAppeared());
            assertNull(output.getWildForestReindeerAppeared());
            assertNull(output.getFallowDeerAppeared());

            assertNull(output.getEstimatedSpecimenAmountOfWhiteTailedDeer());
            assertNull(output.getEstimatedSpecimenAmountOfRoeDeer());
            assertNull(output.getEstimatedSpecimenAmountOfWildForestReindeer());
            assertNull(output.getEstimatedSpecimenAmountOfFallowDeer());

            assertNull(output.getTrendOfWhiteTailedDeerPopulationGrowth());
            assertNull(output.getTrendOfRoeDeerPopulationGrowth());
            assertNull(output.getTrendOfWildForestReindeerPopulationGrowth());
            assertNull(output.getTrendOfFallowDeerPopulationGrowth());
        });
    }

    @Test
    public void testValidate_withOutOfRangeNumericValues() {
        final MooseDataCardPage7 input = new MooseDataCardPage7()
                .withEstimatedSpecimenAmountOfWhiteTailedDeer(-1)
                .withEstimatedSpecimenAmountOfRoeDeer(-1)
                .withEstimatedSpecimenAmountOfWildForestReindeer(-1)
                .withEstimatedSpecimenAmountOfFallowDeer(-1);

        final Validation<List<String>, MooseDataCardPage7> validation = validate(input);
        assertTrue(validation.isValid());

        validation.peek(output -> {
            assertNull(output.getEstimatedSpecimenAmountOfWhiteTailedDeer());
            assertNull(output.getEstimatedSpecimenAmountOfRoeDeer());
            assertNull(output.getEstimatedSpecimenAmountOfWildForestReindeer());
            assertNull(output.getEstimatedSpecimenAmountOfFallowDeer());
        });
    }

    @Test
    public void testValidate_withInvalidTrendValues() {
        final String invalid = "invalid";

        final MooseDataCardPage7 input = new MooseDataCardPage7()
                .withTrendOfWhiteTailedDeerPopulationGrowth(invalid)
                .withTrendOfRoeDeerPopulationGrowth(invalid)
                .withTrendOfWildForestReindeerPopulationGrowth(invalid)
                .withTrendOfFallowDeerPopulationGrowth(invalid);

        final Validation<List<String>, MooseDataCardPage7> validation = validate(input);
        assertTrue(validation.isValid());

        validation.peek(output -> {
            assertNull(output.getTrendOfWhiteTailedDeerPopulationGrowth());
            assertNull(output.getTrendOfRoeDeerPopulationGrowth());
            assertNull(output.getTrendOfWildForestReindeerPopulationGrowth());
            assertNull(output.getTrendOfFallowDeerPopulationGrowth());
        });
    }

    @Test
    public void testValidate_whenSpeciesMarkedNotAppearing() {
        Stream.of(MooseDataCardGameSpeciesAppearance.NO).forEach(appearance -> {
            final MooseDataCardPage7 input = newPage7()
                    .withWhiteTailedDeerAppeared(appearance)
                    .withRoeDeerAppeared(appearance)
                    .withWildForestReindeerAppeared(appearance)
                    .withFallowDeerAppeared(appearance);

            final Validation<List<String>, MooseDataCardPage7> validation = validate(input);
            assertTrue(validation.isValid());

            validation.peek(output -> {
                assertEquals(appearance, output.getWhiteTailedDeerAppeared());
                assertEquals(appearance, output.getRoeDeerAppeared());
                assertEquals(appearance, output.getWildForestReindeerAppeared());
                assertEquals(appearance, output.getFallowDeerAppeared());

                assertNull(output.getEstimatedSpecimenAmountOfWhiteTailedDeer());
                assertNull(output.getEstimatedSpecimenAmountOfRoeDeer());
                assertNull(output.getEstimatedSpecimenAmountOfWildForestReindeer());
                assertNull(output.getEstimatedSpecimenAmountOfFallowDeer());

                assertNull(output.getTrendOfWhiteTailedDeerPopulationGrowth());
                assertNull(output.getTrendOfRoeDeerPopulationGrowth());
                assertNull(output.getTrendOfWildForestReindeerPopulationGrowth());
                assertNull(output.getTrendOfFallowDeerPopulationGrowth());
            });
        });
    }

}
