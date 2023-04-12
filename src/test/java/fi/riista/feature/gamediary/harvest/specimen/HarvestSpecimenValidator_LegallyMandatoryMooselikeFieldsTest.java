package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.HasGameSpeciesCode;
import fi.riista.feature.gamediary.fixture.HarvestSpecimenType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.fields.LegallyMandatoryFieldsMooselike;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestSpecimenField;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING;
import static fi.riista.util.DateUtil.huntingYear;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.spy;

@RunWith(Parameterized.class)
public class HarvestSpecimenValidator_LegallyMandatoryMooselikeFieldsTest
        implements HasGameSpeciesCode, ValueGeneratorMixin {

    private static class Tuple implements HasGameSpeciesCode {

        private final int huntingYear;
        private final int speciesCode;
        private final HarvestSpecimenType specimenType;
        private final HarvestSpecVersion specVersion;
        private final boolean withPermit;

        Tuple(final int huntingYear,
              final int speciesCode,
              final HarvestSpecimenType specimenType,
              final HarvestSpecVersion specVersion,
              final boolean withPermit) {

            this.huntingYear = huntingYear;
            this.speciesCode = speciesCode;
            this.specimenType = specimenType;
            this.specVersion = specVersion;
            this.withPermit = withPermit;
        }

        @Override
        public int getGameSpeciesCode() {
            return speciesCode;
        }

        public boolean isValidCombination() {
            return specimenType != HarvestSpecimenType.ANTLERS_LOST
                    || specVersion.supportsAntlerFields2020() && huntingYear >= 2020;
        }

        public Object[] toObjectArray() {
            return new Object[]{huntingYear, speciesCode, specimenType, specVersion, withPermit};
        }
    }

    @Parameters(name = "{index}: huntingYear={0}; speciesCode={1}; specimenType={2}; specVersion={3}")
    public static Iterable<Object[]> data() {
        return IntStream
                .rangeClosed(2016, huntingYear())
                .boxed()
                .flatMap(huntingYear -> MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING
                        .stream()
                        .flatMap(speciesCode -> Stream.of(true, false)
                                .flatMap(withPermit -> Arrays.stream(HarvestSpecimenType.values())
                                        .flatMap(specimenType -> Stream.of(
                                                        HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020,
                                                        HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_MANDATORY_AGE_AND_GENDER_FIELDS_FOR_MOOSELIKE_HARVEST)

                                                .map(specVersion ->
                                                        new Tuple(huntingYear,
                                                                speciesCode,
                                                                specimenType,
                                                                specVersion,
                                                                withPermit)
                                                )))))
                .filter(Tuple::isValidCombination)
                .map(Tuple::toObjectArray)
                .collect(toList());
    }

    @Parameter(0)
    public int huntingYear;

    @Parameter(1)
    public int gameSpeciesCode;

    @Parameter(2)
    public HarvestSpecimenType specimenType;

    @Parameter(3)
    public HarvestSpecVersion specVersion;

    @Parameter(4)
    public boolean withPermit;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private HarvestSpecimen specimen;

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    @Override
    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    @Before
    public void setup() {
        // Mocking needed because JPA static metamodel is not available in unit tests.
        specimen = spy(new HarvestSpecimen());
        specimen.setAge(specimenType.getAge());
        specimen.setGender(specimenType.getGender());

        if (specimenType.isAntlersLostDefined()) {
            specimen.setAntlersLost(specimenType.isAntlersLost());
        }
    }

    private void test(final Consumer<HarvestSpecimenValidator> consumer) {
        final RequiredHarvestFields.Specimen specimenRequirements = LegallyMandatoryFieldsMooselike
                .getSpecimenFields(huntingYear, gameSpeciesCode, specVersion);

        final HarvestSpecimenValidator builder = new HarvestSpecimenValidator(
                specimenRequirements, specimen, gameSpeciesCode, true, specVersion, withPermit);

        consumer.accept(builder);
        builder.throwOnErrors();
    }

    @Test
    public void testValidateAge_whenMissing() {
        assumeTrue(specimen.getAge() == null);
        expectMissing(HarvestSpecimenFieldName.AGE);

        test(HarvestSpecimenValidator::validateAge);
    }

    @Test
    public void testValidateAge_whenNotValidForClubHunting() {
        assumeTrue(specimenType.isAgeUnknown());

        if (!(specVersion.supportsMandatoryAgeAndGenderFieldsForMooselikeHarvest() && withPermit)) {
            expectInvalid(HarvestSpecimenFieldName.AGE, GameAge.UNKNOWN);
        }

        test(HarvestSpecimenValidator::validateAge);
    }

    @Test
    public void testValidateGender_whenMissing() {
        assumeFalse(specimenType.isGenderPresent());

        expectMissing(HarvestSpecimenFieldName.GENDER);

        test(HarvestSpecimenValidator::validateGender);
    }

    @Test
    public void testValidateWeight_whenEstimatedAndMeasuredWeightAreMissing() {
        test(HarvestSpecimenValidator::validateMooselikeWeight);
    }

    @Test
    public void testValidateWeight_whenEstimatedWeightIsGiven() {
        specimen.setWeightEstimated(123.);
        specimen.setWeightMeasured(null);

        test(HarvestSpecimenValidator::validateMooselikeWeight);
    }

    @Test
    public void testValidateWeight_whenMeasuredWeightIsGiven() {
        specimen.setWeightEstimated(null);
        specimen.setWeightMeasured(123.4);

        test(HarvestSpecimenValidator::validateMooselikeWeight);
    }

    @Test
    public void testValidateNotEdible_whenGiven() {
        specimen.setNotEdible(someBoolean());
        test(HarvestSpecimenValidator::validateNotEdible);
    }

    @Test
    public void testValidateNotEdible_whenMissing() {
        test(HarvestSpecimenValidator::validateNotEdible);
    }

    @Test
    public void testValidateFitnessClass_whenGiven() {
        if (!isMoose()) {
            expectIllegal(HarvestSpecimenFieldName.FITNESS_CLASS);
        }

        specimen.setFitnessClass(some(GameFitnessClass.class));
        test(HarvestSpecimenValidator::validateFitnessClass);
    }

    @Test
    public void testValidateFitnessClass_whenMissing() {
        test(HarvestSpecimenValidator::validateFitnessClass);
    }

    @Test
    public void testValidateAntlersLost_whenGiven() {
        assumeFalse(specimenType == HarvestSpecimenType.ANTLERS_LOST);

        if (!specVersion.supportsAntlerFields2020() || huntingYear < 2020 || !specimenType.isAdultMale()) {
            expectIllegal(HarvestSpecimenFieldName.ANTLERS_LOST);
        }

        specimen.setAntlersLost(someBoolean());
        test(HarvestSpecimenValidator::validateAntlersLost);
    }

    @Test
    public void testValidateAntlersLost_whenMissing() {
        if (specVersion.supportsAntlerFields2020() && huntingYear >= 2020 && specimenType.isAdultMale()) {
            expectMissing(HarvestSpecimenFieldName.ANTLERS_LOST);
        }

        specimen.setAntlersLost(null);
        test(HarvestSpecimenValidator::validateAntlersLost);
    }

    @Test
    public void testValidateAntlersType_whenGiven() {
        if (specVersion.supportsAntlerFields2020() && huntingYear >= 2020) {
            if (!isMoose() || !specimenType.isAdultMaleAndAntlersPresent()) {
                expectIllegal(HarvestSpecimenFieldName.ANTLERS_TYPE);
            }
        } else if (!isMoose() || !specimenType.isAdultMale()) {
            expectIllegal(HarvestSpecimenFieldName.ANTLERS_TYPE);
        }

        specimen.setAntlersType(some(GameAntlersType.class));
        test(HarvestSpecimenValidator::validateAntlersType);
    }

    @Test
    public void testValidateAntlersType_whenMissing() {
        test(HarvestSpecimenValidator::validateAntlersType);
    }

    @Test
    public void testValidateAntlersWidth_whenGiven() {
        if (specVersion.supportsAntlerFields2020() && huntingYear >= 2020) {
            if (!(isFallowDeer() || isMoose() || isWhiteTailedDeer() || isWildForestReindeer())
                    || !specimenType.isAdultMaleAndAntlersPresent()) {

                expectIllegal(HarvestSpecimenFieldName.ANTLERS_WIDTH);
            }
        } else if (!isMooseOrDeerRequiringPermitForHunting() || !specimenType.isAdultMale()) {
            expectIllegal(HarvestSpecimenFieldName.ANTLERS_WIDTH);
        }

        specimen.setAntlersWidth(50);
        test(HarvestSpecimenValidator::validateAntlersWidth);
    }

    @Test
    public void testValidateAntlersWidth_whenMissing() {
        test(HarvestSpecimenValidator::validateAntlersWidth);
    }

    @Test
    public void testValidateAntlerPointsLeft_whenGiven() {
        if (!specimenType.isAdultMaleAndAntlersPresent()) {
            expectIllegal(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT);
        }

        specimen.setAntlerPointsLeft(10);
        test(HarvestSpecimenValidator::validateAntlerPointsLeft);
    }

    @Test
    public void testValidateAntlerPointsLeft_whenMissing() {
        test(HarvestSpecimenValidator::validateAntlerPointsLeft);
    }

    @Test
    public void testValidateAntlerPointsRight_whenGiven() {
        if (!specimenType.isAdultMaleAndAntlersPresent()) {
            expectIllegal(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT);
        }

        specimen.setAntlerPointsRight(10);
        test(HarvestSpecimenValidator::validateAntlerPointsRight);
    }

    @Test
    public void testValidateAntlerPointsRight_whenMissing() {
        test(HarvestSpecimenValidator::validateAntlerPointsRight);
    }

    @Test
    public void testValidateAntlersGirth_whenGiven() {
        if (!specVersion.supportsAntlerFields2020()
                || huntingYear < 2020
                || !(isMoose() || isWhiteTailedDeer())
                || !specimenType.isAdultMaleAndAntlersPresent()) {

            expectIllegal(HarvestSpecimenFieldName.ANTLERS_GIRTH);
        }

        specimen.setAntlersGirth(50);
        test(HarvestSpecimenValidator::validateAntlersGirth);
    }

    @Test
    public void testValidateAntlersGirth_whenMissing() {
        test(HarvestSpecimenValidator::validateAntlersGirth);
    }

    @Test
    public void testValidateAntlersLength_whenGiven() {
        if (!specVersion.supportsAntlerFields2020()
                || huntingYear < 2020
                || !(isRoeDeer() || isWhiteTailedDeer())
                || !specimenType.isAdultMaleAndAntlersPresent()) {

            expectIllegal(HarvestSpecimenFieldName.ANTLERS_LENGTH);
        }

        specimen.setAntlersLength(50);
        test(HarvestSpecimenValidator::validateAntlersLength);
    }

    @Test
    public void testValidateAntlersLength_whenMissing() {
        test(HarvestSpecimenValidator::validateAntlersLength);
    }

    @Test
    public void testValidateAntlersInnerWidth_whenGiven() {
        if (!specVersion.supportsAntlerFields2020()
                || huntingYear < 2020
                || !isWhiteTailedDeer()
                || !specimenType.isAdultMaleAndAntlersPresent()) {

            expectIllegal(HarvestSpecimenFieldName.ANTLERS_INNER_WIDTH);
        }

        specimen.setAntlersInnerWidth(50);
        test(HarvestSpecimenValidator::validateAntlersInnerWidth);
    }

    @Test
    public void testValidateAntlersInnerWidth_whenMissing() {
        test(HarvestSpecimenValidator::validateAntlersInnerWidth);
    }

    @Test
    public void testValidateAntlerShaftWidth_whenGiven() {
        expectIllegal(HarvestSpecimenFieldName.ANTLER_SHAFT_WIDTH);

        specimen.setAntlerShaftWidth(50);
        test(HarvestSpecimenValidator::validateAntlerShaftWidth);
    }

    @Test
    public void testValidateAntlerShaftWidth_whenMissing() {
        test(HarvestSpecimenValidator::validateAntlerShaftWidth);
    }

    @Test
    public void testValidateAlone_whenGiven() {
        if (!isMoose() || !specimenType.isYoung()) {
            expectIllegal(HarvestSpecimenFieldName.ALONE);
        }

        specimen.setAlone(someBoolean());
        test(HarvestSpecimenValidator::validateAlone);
    }

    @Test
    public void testValidateAlone_whenMissing() {
        test(HarvestSpecimenValidator::validateAlone);
    }

    private void expectMissing(final HarvestSpecimenFieldName fieldName) {
        thrown.expectMessage("missing " + fieldName.name());
    }

    private void expectIllegal(final HarvestSpecimenFieldName fieldName) {
        thrown.expectMessage("illegal " + fieldName.name());
    }

    private void expectInvalid(final HarvestSpecimenFieldName fieldName, final Object value) {
        thrown.expectMessage("invalid " + fieldName.name() + ": " + value);
    }
}
