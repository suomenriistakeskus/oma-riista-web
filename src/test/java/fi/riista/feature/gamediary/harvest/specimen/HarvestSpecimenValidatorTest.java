package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.HasGameSpeciesCode;
import fi.riista.feature.gamediary.fixture.HarvestSpecimenType;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsImpl;
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

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GREY_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static fi.riista.util.DateUtil.huntingYear;
import static java.util.stream.Collectors.toList;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.spy;

@RunWith(Parameterized.class)
public class HarvestSpecimenValidatorTest implements HasGameSpeciesCode, ValueGeneratorMixin {

    private static class Tuple implements HasGameSpeciesCode {

        private final int huntingYear;
        private final int speciesCode;
        private final HarvestSpecimenType specimenType;
        private final boolean linkedToHuntingDay;
        private final boolean isClientSupportFor2020Fields;

        Tuple(final int huntingYear,
              final int speciesCode,
              final HarvestSpecimenType specimenType,
              final boolean linkedToHuntingDay,
              final boolean isClientSupportFor2020Fields) {

            this.huntingYear = huntingYear;
            this.speciesCode = speciesCode;
            this.specimenType = specimenType;
            this.linkedToHuntingDay = linkedToHuntingDay;
            this.isClientSupportFor2020Fields = isClientSupportFor2020Fields;
        }

        @Override
        public int getGameSpeciesCode() {
            return speciesCode;
        }

        public boolean isValidCombination() {
            if (linkedToHuntingDay && !isMooseOrDeerRequiringPermitForHunting()) {
                return false;
            }

            return specimenType != HarvestSpecimenType.ANTLERS_LOST
                    || isClientSupportFor2020Fields && huntingYear >= 2020;
        }

        public Object[] toObjectArray() {
            return new Object[]{huntingYear, speciesCode, specimenType, linkedToHuntingDay, isClientSupportFor2020Fields};
        }
    }

    @Parameters(name = "{index}: huntingYear={0}; speciesCode={1}; specimenType={2}; linkedToHuntingDay={3}, isClientSupportFor2020Fields={4}")
    public static Iterable<Object[]> data() {
        return IntStream
                .rangeClosed(2016, huntingYear())
                .boxed()
                .flatMap(huntingYear -> IntStream
                        .of(OFFICIAL_CODE_BEAR, OFFICIAL_CODE_FALLOW_DEER, OFFICIAL_CODE_GREY_SEAL, OFFICIAL_CODE_MOOSE,
                                OFFICIAL_CODE_ROE_DEER, OFFICIAL_CODE_WHITE_TAILED_DEER, OFFICIAL_CODE_WILD_BOAR,
                                OFFICIAL_CODE_WILD_FOREST_REINDEER)
                        .boxed()
                        .flatMap(speciesCode -> Arrays.stream(HarvestSpecimenType.values())
                                .flatMap(specimenType -> Stream.of(true, false)
                                        .flatMap(linkedToHuntingDay -> Stream.of(true, false)
                                                .map(isClientSupportFor2020Fields ->
                                                        new Tuple(huntingYear,
                                                                speciesCode,
                                                                specimenType,
                                                                linkedToHuntingDay,
                                                                isClientSupportFor2020Fields)
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
    public boolean linkedToHuntingDay;

    @Parameter(4)
    public boolean isClientSupportFor2020Fields;

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
        // Mocking needed becase JPA static metamodel is not available in unit tests.
        specimen = spy(new HarvestSpecimen());
        specimen.setAge(specimenType.getAge());
        specimen.setGender(specimenType.getGender());

        if (specimenType.isAntlersLostDefined()) {
            specimen.setAntlersLost(specimenType.isAntlersLost());
        }
    }

    private void test(final Consumer<HarvestSpecimenValidator> consumer) {
        final HarvestReportingType reportingType =
                linkedToHuntingDay ? HarvestReportingType.HUNTING_DAY : HarvestReportingType.BASIC;

        final RequiredHarvestFields.Specimen specimenRequirements = RequiredHarvestFieldsImpl.getSpecimenFields(
                huntingYear, gameSpeciesCode, null, reportingType, isClientSupportFor2020Fields);

        final HarvestSpecimenValidator builder = new HarvestSpecimenValidator(
                specimenRequirements, specimen, gameSpeciesCode, linkedToHuntingDay);

        consumer.accept(builder);
        builder.throwOnErrors();
    }

    @Test
    public void testValidateAge_whenMissing() {
        assumeFalse(specimenType.isAgePresent());

        if (linkedToHuntingDay) {
            expectMissing(HarvestSpecimenFieldName.AGE);
        }

        test(HarvestSpecimenValidator::validateAge);
    }

    @Test
    public void testValidateAge_whenNotValidForClubHunting() {
        assumeTrue(isMooseOrDeerRequiringPermitForHunting() && specimenType.isAgeUnknown());

        if (linkedToHuntingDay) {
            expectInvalid(HarvestSpecimenFieldName.AGE, GameAge.UNKNOWN);
        }

        test(HarvestSpecimenValidator::validateAge);
    }

    @Test
    public void testValidateGender_whenMissing() {
        assumeFalse(specimenType.isGenderPresent());

        if (linkedToHuntingDay) {
            expectMissing(HarvestSpecimenFieldName.GENDER);
        }

        test(HarvestSpecimenValidator::validateGender);
    }

    @Test
    public void testValidateGender_whenNotValidForClubHunting() {
        assumeTrue(isMooseOrDeerRequiringPermitForHunting() && specimenType.isGenderUnknown());

        if (linkedToHuntingDay) {
            expectInvalid(HarvestSpecimenFieldName.GENDER, GameGender.UNKNOWN);
        }

        test(HarvestSpecimenValidator::validateGender);
    }

    @Test
    public void testValidateWeight_whenEstimatedAndMeasuredWeightAreMissing() {
        test(HarvestSpecimenValidator::validateMooselikeWeight);
    }

    @Test
    public void testValidateWeight_whenEstimatedWeightIsGiven() {
        if (huntingYear < 2020 && !isMooseOrDeerRequiringPermitForHunting()
                || huntingYear >= 2020 && !(isMooselike() || isWildBoar())) {

            expectIllegal(HarvestSpecimenFieldName.WEIGHT_ESTIMATED);
        }

        specimen.setWeightEstimated(123.4);
        specimen.setWeightMeasured(null);

        test(HarvestSpecimenValidator::validateMooselikeWeight);
    }

    @Test
    public void testValidateWeight_whenMeasuredWeightIsGiven() {
        if ((!isClientSupportFor2020Fields || huntingYear < 2020) && !isMooseOrDeerRequiringPermitForHunting()
                || isClientSupportFor2020Fields && huntingYear >= 2020 && !(isMooselike() || isWildBoar())) {

            expectIllegal(HarvestSpecimenFieldName.WEIGHT_MEASURED);
        }

        specimen.setWeightEstimated(null);
        specimen.setWeightMeasured(123.4);

        test(HarvestSpecimenValidator::validateMooselikeWeight);
    }

    @Test
    public void testValidateNotEdible_whenGiven() {
        if (!isMooseOrDeerRequiringPermitForHunting()) {
            expectIllegal(HarvestSpecimenFieldName.NOT_EDIBLE);
        }

        specimen.setNotEdible(someBoolean());
        test(HarvestSpecimenValidator::validateNotEdible);
    }

    @Test
    public void testValidateNotEdible_whenMissing() {
        if (isMoose() && linkedToHuntingDay) {
            expectMissing(HarvestSpecimenFieldName.NOT_EDIBLE);
        }

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
        if (!isClientSupportFor2020Fields || huntingYear < 2020 || !isMooselike() || !specimenType.isAdultMale()) {
            expectIllegal(HarvestSpecimenFieldName.ANTLERS_LOST);
        }

        specimen.setAntlersLost(someBoolean());
        test(HarvestSpecimenValidator::validateAntlersLost);
    }

    @Test
    public void testValidateAntlersLost_whenMissing() {
        assumeFalse(specimenType.isAntlersLost());

        if (isClientSupportFor2020Fields
                && huntingYear >= 2020
                && isMooselike()
                && specimenType.isAdultMale()
                && linkedToHuntingDay) {

            expectMissing(HarvestSpecimenFieldName.ANTLERS_LOST);
        }

        specimen.setAntlersLost(null);
        test(HarvestSpecimenValidator::validateAntlersLost);
    }

    @Test
    public void testValidateAntlersType_whenGiven() {
        if (!isMoose() || !specimenType.isAdultMaleAndAntlersPresent()) {
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
        if (!specimenType.isAdultMaleAndAntlersPresent()
                || (!isClientSupportFor2020Fields || huntingYear < 2020) && !isMooseOrDeerRequiringPermitForHunting()
                || isClientSupportFor2020Fields && huntingYear >= 2020 && !isMooseOrDeerRequiringPermitForHunting()) {

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
        if (!specimenType.isAdultMaleAndAntlersPresent()
                || (!isClientSupportFor2020Fields || huntingYear < 2020) && !isMooseOrDeerRequiringPermitForHunting()
                || isClientSupportFor2020Fields && huntingYear >= 2020 && !isMooselike()) {

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
        if (!specimenType.isAdultMaleAndAntlersPresent()
                || (!isClientSupportFor2020Fields || huntingYear < 2020) && !isMooseOrDeerRequiringPermitForHunting()
                || isClientSupportFor2020Fields && huntingYear >= 2020 && !isMooselike()) {

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
        if (!isClientSupportFor2020Fields
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
        if (!isClientSupportFor2020Fields
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
        if (!isClientSupportFor2020Fields
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
        if (!isClientSupportFor2020Fields
                || huntingYear < 2020
                || !isRoeDeer()
                || !specimenType.isAdultMaleAndAntlersPresent()) {

            expectIllegal(HarvestSpecimenFieldName.ANTLER_SHAFT_WIDTH);
        }

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
