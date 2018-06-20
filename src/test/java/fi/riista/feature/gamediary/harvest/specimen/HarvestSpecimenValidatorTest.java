package fi.riista.feature.gamediary.harvest.specimen;

import com.google.common.collect.ObjectArrays;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
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
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.isMoose;
import static fi.riista.feature.gamediary.GameSpecies.isMooseOrDeerRequiringPermitForHunting;
import static java.util.stream.Collectors.toList;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.spy;

@RunWith(Parameterized.class)
public class HarvestSpecimenValidatorTest implements ValueGeneratorMixin {

    private static final int HUNTING_YEAR = 2017;

    @Parameters(name = "{index}: speciesCode={0}; age={1}; gender={2}; linkedToHuntingDay={3}")
    public static Iterable<Object[]> data() {
        final GameAge[] ageOptions = ObjectArrays.<GameAge>concat(null, GameAge.values());
        final GameGender[] genderOptions = ObjectArrays.<GameGender>concat(null, GameGender.values());

        return IntStream
                .of(OFFICIAL_CODE_BEAR, OFFICIAL_CODE_MOOSE, OFFICIAL_CODE_WHITE_TAILED_DEER)
                .boxed()
                .flatMap(speciesCode -> Arrays.stream(ageOptions)
                        .flatMap(age -> Arrays.stream(genderOptions)
                                .flatMap(gender -> Stream.of(true, false)
                                        .map(linkedToHuntingDay -> {
                                            return new Object[]{speciesCode, age, gender, linkedToHuntingDay};
                                        }))))
                .collect(toList());
    }

    @Parameter(0)
    public int gameSpeciesCode;

    @Parameter(1)
    public GameAge age;

    @Parameter(2)
    public GameGender gender;

    @Parameter(3)
    public boolean linkedToHuntingDay;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private HarvestSpecimen specimen;

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    @Before
    public void setup() {
        // Mocking needed becase JPA static metamodel is not available.
        specimen = spy(new HarvestSpecimen());
        specimen.setAge(age);
        specimen.setGender(gender);
    }

    private void test(final Consumer<HarvestSpecimenValidator> consumer) {
        final HarvestReportingType reportingType = linkedToHuntingDay
                ? HarvestReportingType.HUNTING_DAY
                : HarvestReportingType.BASIC;
        final RequiredHarvestFields.Specimen specimenRequirements =
                RequiredHarvestFields.getSpecimenFields(HUNTING_YEAR, gameSpeciesCode, null, reportingType);
        final HarvestSpecimenValidator builder = new HarvestSpecimenValidator(
                specimenRequirements, specimen, gameSpeciesCode, linkedToHuntingDay);
        consumer.accept(builder);
        builder.throwOnErrors();
    }

    @Test
    public void testValidateAge_whenMissing() {
        assumeTrue(specimen.getAge() == null);

        if (linkedToHuntingDay) {
            expectMissing(HarvestSpecimenFieldName.AGE);
        }

        test(HarvestSpecimenValidator::validateAge);
    }

    @Test
    public void testValidateAge_whenNotValidForClubHunting() {
        assumeTrue(specimen.getAge() == GameAge.UNKNOWN);

        if (linkedToHuntingDay && gameSpeciesCode != OFFICIAL_CODE_BEAR) {
            expectInvalid(HarvestSpecimenFieldName.AGE, GameAge.UNKNOWN);
        }

        test(HarvestSpecimenValidator::validateAge);
    }

    @Test
    public void testValidateGender_whenMissing() {
        assumeTrue(specimen.getGender() == null);

        if (linkedToHuntingDay) {
            expectMissing(HarvestSpecimenFieldName.GENDER);
        }

        test(HarvestSpecimenValidator::validateGender);
    }

    @Test
    public void testValidateGender_whenNotValidForClubHunting() {
        assumeTrue(specimen.getGender() == GameGender.UNKNOWN);

        if (linkedToHuntingDay && gameSpeciesCode != OFFICIAL_CODE_BEAR) {
            expectInvalid(HarvestSpecimenFieldName.GENDER, GameGender.UNKNOWN);
        }

        test(HarvestSpecimenValidator::validateGender);
    }

    @Test
    public void testValidateWeight_whenEstimatedAndMeasuredWeightAreMissing() {
        if (isMoose(gameSpeciesCode) && linkedToHuntingDay) {
            thrown.expectMessage("missing both estimated and measured weight");
        }

        test(HarvestSpecimenValidator::validateMooselikeWeight);
    }

    @Test
    public void testValidateWeight_whenEstimatedWeightIsGiven() {
        if (!isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode)) {
            expectIllegal(HarvestSpecimenFieldName.WEIGHT_ESTIMATED);
        }

        specimen.setWeightEstimated(123.);
        specimen.setWeightMeasured(null);

        test(HarvestSpecimenValidator::validateMooselikeWeight);
    }

    @Test
    public void testValidateWeight_whenMeasuredWeightIsGiven() {
        if (!isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode)) {
            expectIllegal(HarvestSpecimenFieldName.WEIGHT_MEASURED);
        }

        specimen.setWeightEstimated(null);
        specimen.setWeightMeasured(123.4);

        test(HarvestSpecimenValidator::validateMooselikeWeight);
    }

    @Test
    public void testValidateNotEdible_whenGiven() {
        if (!isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode)) {
            expectIllegal(HarvestSpecimenFieldName.NOT_EDIBLE);
        }

        specimen.setNotEdible(someBoolean());
        test(HarvestSpecimenValidator::validateNotEdible);
    }

    @Test
    public void testValidateNotEdible_whenMissing() {
        if (isMoose(gameSpeciesCode) && linkedToHuntingDay) {
            expectMissing(HarvestSpecimenFieldName.NOT_EDIBLE);
        }

        test(HarvestSpecimenValidator::validateNotEdible);
    }

    @Test
    public void testValidateFitnessClass_whenGiven() {
        if (!isMoose(gameSpeciesCode)) {
            expectIllegal(HarvestSpecimenFieldName.FITNESS_CLASS);
        }

        specimen.setFitnessClass(some(GameFitnessClass.class));
        test(HarvestSpecimenValidator::validateFitnessClass);
    }

    @Test
    public void testValidateFitnessClass_whenMissing() {
        if (isMoose(gameSpeciesCode) && linkedToHuntingDay) {
            expectMissing(HarvestSpecimenFieldName.FITNESS_CLASS);
        }

        test(HarvestSpecimenValidator::validateFitnessClass);
    }

    @Test
    public void testValidateAntlersWidth_whenGiven() {
        if (!isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode) || !isAdult() || !isMale()) {
            expectIllegal(HarvestSpecimenFieldName.ANTLERS_WIDTH);
        }

        specimen.setAntlersWidth(50);
        test(HarvestSpecimenValidator::validateAntlersWidth);
    }

    @Test
    public void testValidateAntlersWidth_whenMissing() {
        if (isMoose(gameSpeciesCode) && isAdult() && isMale() && linkedToHuntingDay) {
            expectMissing(HarvestSpecimenFieldName.ANTLERS_WIDTH);
        }

        test(HarvestSpecimenValidator::validateAntlersWidth);
    }

    @Test
    public void testValidateAntlerPointsLeft_whenGiven() {
        if (!isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode) || !isAdult() || !isMale()) {
            expectIllegal(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT);
        }

        specimen.setAntlerPointsLeft(10);
        test(HarvestSpecimenValidator::validateAntlerPointsLeft);
    }

    @Test
    public void testValidateAntlerPointsLeft_whenMissing() {
        if (isMoose(gameSpeciesCode) && isAdult() && isMale() && linkedToHuntingDay) {
            expectMissing(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT);
        }

        test(HarvestSpecimenValidator::validateAntlerPointsLeft);
    }

    @Test
    public void testValidateAntlerPointsRight_whenGiven() {
        if (!isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode) || !isAdult() || !isMale()) {
            expectIllegal(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT);
        }

        specimen.setAntlerPointsRight(10);
        test(HarvestSpecimenValidator::validateAntlerPointsRight);
    }

    @Test
    public void testValidateAntlerPointsRight_whenMissing() {
        if (isMoose(gameSpeciesCode) && isAdult() && isMale() && linkedToHuntingDay) {
            expectMissing(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT);
        }

        test(HarvestSpecimenValidator::validateAntlerPointsRight);
    }

    @Test
    public void testValidateAntlersType_whenGiven() {
        if (!isMoose(gameSpeciesCode) || !isAdult() || !isMale()) {
            expectIllegal(HarvestSpecimenFieldName.ANTLERS_TYPE);
        }

        specimen.setAntlersType(some(GameAntlersType.class));
        test(HarvestSpecimenValidator::validateAntlersType);
    }

    @Test
    public void testValidateAntlersType_whenMissing() {
        if (isMoose(gameSpeciesCode) && isAdult() && isMale() && linkedToHuntingDay) {
            expectMissing(HarvestSpecimenFieldName.ANTLERS_TYPE);
        }

        test(HarvestSpecimenValidator::validateAntlersType);
    }

    @Test
    public void testValidateAlone_whenGiven() {
        if (!isMoose(gameSpeciesCode) || !isYoung()) {
            expectIllegal(HarvestSpecimenFieldName.ALONE);
        }

        specimen.setAlone(someBoolean());
        test(HarvestSpecimenValidator::validateAlone);
    }

    @Test
    public void testValidateAlone_whenMissing() {
        if (isMoose(gameSpeciesCode) && isYoung() && linkedToHuntingDay) {
            expectMissing(HarvestSpecimenFieldName.ALONE);
        }

        test(HarvestSpecimenValidator::validateAlone);
    }

    private boolean isAdult() {
        return age == GameAge.ADULT;
    }

    private boolean isYoung() {
        return age == GameAge.YOUNG;
    }

    private boolean isMale() {
        return gender == GameGender.MALE;
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
