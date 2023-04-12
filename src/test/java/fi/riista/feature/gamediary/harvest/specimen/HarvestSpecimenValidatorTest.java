package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.HasGameSpeciesCode;
import fi.riista.feature.gamediary.fixture.HarvestSpecimenType;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsImpl;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
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

@RunWith(Theories.class)
public class HarvestSpecimenValidatorTest implements HasGameSpeciesCode, ValueGeneratorMixin {

    public int huntingYear;
    public int gameSpeciesCode;
    public HarvestSpecimenType specimenType;
    public boolean linkedToHuntingDay;
    public HarvestSpecVersion specVersion;

    @DataPoints("huntingYear")
    public static final List<Integer> HUNTING_YEARS = IntStream.rangeClosed(2016, huntingYear())
            .boxed()
            .collect(toList());

    @DataPoints("gameSpeciesCode")
    public static final List<Integer> SPECIES_CODES = IntStream
            .of(OFFICIAL_CODE_BEAR, OFFICIAL_CODE_FALLOW_DEER, OFFICIAL_CODE_GREY_SEAL, OFFICIAL_CODE_MOOSE,
                    OFFICIAL_CODE_ROE_DEER, OFFICIAL_CODE_WHITE_TAILED_DEER, OFFICIAL_CODE_WILD_BOAR,
                    OFFICIAL_CODE_WILD_FOREST_REINDEER)
            .boxed()
            .collect(toList());

    @DataPoints("specimenType")
    public static final List<HarvestSpecimenType> SPECIMEN_TYPES = Arrays.stream(HarvestSpecimenType.values())
            .collect(toList());

    @DataPoints("linkedToHuntingDay")
    public static final List<Boolean> LINKED_HUNTING_DAYS = Stream.of(true, false).collect(toList());

    @DataPoints("specVersion")
    public static final List<HarvestSpecVersion> SPEC_VERSIONS = Stream
            .of(HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020,
                    HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_MANDATORY_AGE_AND_GENDER_FIELDS_FOR_MOOSELIKE_HARVEST)
            .collect(toList());

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

    private void setup(final Integer huntingYear,
                       final Integer gameSpeciesCode,
                       final HarvestSpecimenType specimenType,
                       final Boolean linkedToHuntingDay,
                       final HarvestSpecVersion specVersion) {

        this.huntingYear = huntingYear;
        this.gameSpeciesCode = gameSpeciesCode;
        this.specimenType = specimenType;
        this.linkedToHuntingDay = linkedToHuntingDay;
        this.specVersion = specVersion;

        // Preconditions for valid combination
        assumeTrue(specimenType != HarvestSpecimenType.ANTLERS_LOST
                || specVersion.supportsAntlerFields2020() && huntingYear >= 2020);

        // Mocking needed because JPA static metamodel is not available in unit tests.
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
                huntingYear, gameSpeciesCode, null, reportingType, specVersion, linkedToHuntingDay);

        final HarvestSpecimenValidator builder = new HarvestSpecimenValidator(
                specimenRequirements, specimen, gameSpeciesCode, linkedToHuntingDay, specVersion, linkedToHuntingDay);

        consumer.accept(builder);
        builder.throwOnErrors();
    }

    @Theory
    public void testValidateAge_whenMissing(@FromDataPoints("huntingYear") final Integer huntingYear,
                                            @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                            @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                            @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                            @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {

        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        assumeFalse(specimenType.isAgePresent());

        if (isMooseOrDeerRequiringPermitForHunting()) {
            // XOR
            if (linkedToHuntingDay ^ specVersion.supportsMandatoryAgeAndGenderFieldsForMooselikeHarvest()) {
                expectMissing(HarvestSpecimenFieldName.AGE);
            }
        } else if (linkedToHuntingDay) {
            expectMissing(HarvestSpecimenFieldName.AGE);
        }

        test(HarvestSpecimenValidator::validateAge);
    }

    @Theory
    public void testValidateAge_whenNotValidForClubHunting(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                           @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                           @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                           @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                           @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        assumeTrue(isMooseOrDeerRequiringPermitForHunting() && specimenType.isAgeUnknown());

        if (specVersion.supportsMandatoryAgeAndGenderFieldsForMooselikeHarvest()) {
            // basic
            // hunter must give gender and age for mooselike harvest
            if (!linkedToHuntingDay) {
                expectInvalid(HarvestSpecimenFieldName.AGE, GameAge.UNKNOWN);
            }
        } else {
            // old way
            if (linkedToHuntingDay) {
                expectInvalid(HarvestSpecimenFieldName.AGE, GameAge.UNKNOWN);
            }
        }

        test(HarvestSpecimenValidator::validateAge);
    }

    @Theory
    public void testValidateGender_whenMissing(@FromDataPoints("huntingYear") final Integer huntingYear,
                                               @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                               @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                               @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                               @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        assumeFalse(specimenType.isGenderPresent());

        if (isMooseOrDeerRequiringPermitForHunting()) {
            // XOR
            if (linkedToHuntingDay ^ specVersion.supportsMandatoryAgeAndGenderFieldsForMooselikeHarvest()) {
                expectMissing(HarvestSpecimenFieldName.GENDER);
            }
        } else if (linkedToHuntingDay) {
            expectMissing(HarvestSpecimenFieldName.GENDER);
        }

        test(HarvestSpecimenValidator::validateGender);
    }

    @Theory
    public void testValidateGender_whenNotValidForClubHunting(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                              @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                              @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                              @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                              @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        assumeTrue(isMooseOrDeerRequiringPermitForHunting() && specimenType.isGenderUnknown());

        if (specVersion.supportsMandatoryAgeAndGenderFieldsForMooselikeHarvest()) {
            // basic
            // hunter must give gender and age for mooselike harvest
            if (!linkedToHuntingDay) {
                expectInvalid(HarvestSpecimenFieldName.GENDER, GameGender.UNKNOWN);
            }
        } else {
            // old way
            if (linkedToHuntingDay) {
                expectInvalid(HarvestSpecimenFieldName.GENDER, GameGender.UNKNOWN);
            }
        }

        test(HarvestSpecimenValidator::validateGender);
    }

    @Theory
    public void testValidateWeight_whenEstimatedAndMeasuredWeightAreMissing(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                                            @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                                            @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                                            @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                                            @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        test(HarvestSpecimenValidator::validateMooselikeWeight);
    }

    @Theory
    public void testValidateWeight_whenEstimatedWeightIsGiven(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                              @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                              @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                              @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                              @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        if (huntingYear < 2020 && !isMooseOrDeerRequiringPermitForHunting()
                || huntingYear >= 2020 && !(isMooselike() || isWildBoar())) {

            expectIllegal(HarvestSpecimenFieldName.WEIGHT_ESTIMATED);
        }

        specimen.setWeightEstimated(123.4);
        specimen.setWeightMeasured(null);

        test(HarvestSpecimenValidator::validateMooselikeWeight);
    }

    @Theory
    public void testValidateWeight_whenMeasuredWeightIsGiven(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                             @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                             @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                             @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                             @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        if ((!specVersion.supportsAntlerFields2020() || huntingYear < 2020) && !isMooseOrDeerRequiringPermitForHunting()
                || specVersion.supportsAntlerFields2020() && huntingYear >= 2020 && !(isMooselike() || isWildBoar())) {

            expectIllegal(HarvestSpecimenFieldName.WEIGHT_MEASURED);
        }

        specimen.setWeightEstimated(null);
        specimen.setWeightMeasured(123.4);

        test(HarvestSpecimenValidator::validateMooselikeWeight);
    }

    @Theory
    public void testValidateNotEdible_whenGiven(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        if (!isMooseOrDeerRequiringPermitForHunting()) {
            expectIllegal(HarvestSpecimenFieldName.NOT_EDIBLE);
        }

        specimen.setNotEdible(someBoolean());
        test(HarvestSpecimenValidator::validateNotEdible);
    }

    @Theory
    public void testValidateNotEdible_whenMissing(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                  @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                  @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                  @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                  @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        if (isMoose() && linkedToHuntingDay) {
            expectMissing(HarvestSpecimenFieldName.NOT_EDIBLE);
        }

        test(HarvestSpecimenValidator::validateNotEdible);
    }

    @Theory
    public void testValidateFitnessClass_whenGiven(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                   @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                   @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                   @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                   @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        if (!isMoose()) {
            expectIllegal(HarvestSpecimenFieldName.FITNESS_CLASS);
        }

        specimen.setFitnessClass(some(GameFitnessClass.class));
        test(HarvestSpecimenValidator::validateFitnessClass);
    }

    @Theory
    public void testValidateFitnessClass_whenMissing(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                     @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                     @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                     @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                     @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        test(HarvestSpecimenValidator::validateFitnessClass);
    }

    @Theory
    public void testValidateAntlersLost_whenGiven(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                  @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                  @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                  @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                  @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        if (!specVersion.supportsAntlerFields2020() || huntingYear < 2020 || !isMooselike() || !specimenType.isAdultMale()) {
            expectIllegal(HarvestSpecimenFieldName.ANTLERS_LOST);
        }

        specimen.setAntlersLost(someBoolean());
        test(HarvestSpecimenValidator::validateAntlersLost);
    }

    @Theory
    public void testValidateAntlersLost_whenMissing(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                    @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                    @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                    @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                    @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        assumeFalse(specimenType.isAntlersLost());

        if (specVersion.supportsAntlerFields2020()
                && huntingYear >= 2020
                && isMooselike()
                && specimenType.isAdultMale()
                && linkedToHuntingDay) {

            expectMissing(HarvestSpecimenFieldName.ANTLERS_LOST);
        }

        specimen.setAntlersLost(null);
        test(HarvestSpecimenValidator::validateAntlersLost);
    }

    @Theory
    public void testValidateAntlersType_whenGiven(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                  @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                  @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                  @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                  @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        if (!isMoose() || !specimenType.isAdultMaleAndAntlersPresent()) {
            expectIllegal(HarvestSpecimenFieldName.ANTLERS_TYPE);
        }

        specimen.setAntlersType(some(GameAntlersType.class));
        test(HarvestSpecimenValidator::validateAntlersType);
    }

    @Theory
    public void testValidateAntlersType_whenMissing(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                    @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                    @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                    @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                    @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        test(HarvestSpecimenValidator::validateAntlersType);
    }

    @Theory
    public void testValidateAntlersWidth_whenGiven(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                   @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                   @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                   @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                   @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        if (!specimenType.isAdultMaleAndAntlersPresent()
                || (!specVersion.supportsAntlerFields2020() || huntingYear < 2020) && !isMooseOrDeerRequiringPermitForHunting()
                || specVersion.supportsAntlerFields2020() && huntingYear >= 2020 && !isMooseOrDeerRequiringPermitForHunting()) {

            expectIllegal(HarvestSpecimenFieldName.ANTLERS_WIDTH);
        }

        specimen.setAntlersWidth(50);
        test(HarvestSpecimenValidator::validateAntlersWidth);
    }

    @Theory
    public void testValidateAntlersWidth_whenMissing(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                     @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                     @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                     @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                     @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        test(HarvestSpecimenValidator::validateAntlersWidth);
    }

    @Theory
    public void testValidateAntlerPointsLeft_whenGiven(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                       @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                       @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                       @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                       @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        if (!specimenType.isAdultMaleAndAntlersPresent()
                || (!specVersion.supportsAntlerFields2020() || huntingYear < 2020) && !isMooseOrDeerRequiringPermitForHunting()
                || specVersion.supportsAntlerFields2020() && huntingYear >= 2020 && !isMooselike()) {

            expectIllegal(HarvestSpecimenFieldName.ANTLER_POINTS_LEFT);
        }

        specimen.setAntlerPointsLeft(10);
        test(HarvestSpecimenValidator::validateAntlerPointsLeft);
    }

    @Theory
    public void testValidateAntlerPointsLeft_whenMissing(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                         @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                         @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                         @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                         @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        test(HarvestSpecimenValidator::validateAntlerPointsLeft);
    }

    @Theory
    public void testValidateAntlerPointsRight_whenGiven(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                        @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                        @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                        @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                        @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        if (!specimenType.isAdultMaleAndAntlersPresent()
                || (!specVersion.supportsAntlerFields2020() || huntingYear < 2020) && !isMooseOrDeerRequiringPermitForHunting()
                || specVersion.supportsAntlerFields2020() && huntingYear >= 2020 && !isMooselike()) {

            expectIllegal(HarvestSpecimenFieldName.ANTLER_POINTS_RIGHT);
        }

        specimen.setAntlerPointsRight(10);
        test(HarvestSpecimenValidator::validateAntlerPointsRight);
    }

    @Theory
    public void testValidateAntlerPointsRight_whenMissing(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                          @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                          @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                          @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                          @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        test(HarvestSpecimenValidator::validateAntlerPointsRight);
    }

    @Theory
    public void testValidateAntlersGirth_whenGiven(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                   @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                   @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                   @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                   @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        if (!specVersion.supportsAntlerFields2020()
                || huntingYear < 2020
                || !(isMoose() || isWhiteTailedDeer())
                || !specimenType.isAdultMaleAndAntlersPresent()) {

            expectIllegal(HarvestSpecimenFieldName.ANTLERS_GIRTH);
        }

        specimen.setAntlersGirth(50);
        test(HarvestSpecimenValidator::validateAntlersGirth);
    }

    @Theory
    public void testValidateAntlersGirth_whenMissing(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                     @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                     @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                     @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                     @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        test(HarvestSpecimenValidator::validateAntlersGirth);
    }

    @Theory
    public void testValidateAntlersLength_whenGiven(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                    @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                    @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                    @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                    @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        if (!specVersion.supportsAntlerFields2020()
                || huntingYear < 2020
                || !(isRoeDeer() || isWhiteTailedDeer())
                || !specimenType.isAdultMaleAndAntlersPresent()) {

            expectIllegal(HarvestSpecimenFieldName.ANTLERS_LENGTH);
        }

        specimen.setAntlersLength(50);
        test(HarvestSpecimenValidator::validateAntlersLength);
    }

    @Theory
    public void testValidateAntlersLength_whenMissing(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                      @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                      @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                      @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                      @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        test(HarvestSpecimenValidator::validateAntlersLength);
    }

    @Theory
    public void testValidateAntlersInnerWidth_whenGiven(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                        @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                        @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                        @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                        @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        if (!specVersion.supportsAntlerFields2020()
                || huntingYear < 2020
                || !isWhiteTailedDeer()
                || !specimenType.isAdultMaleAndAntlersPresent()) {

            expectIllegal(HarvestSpecimenFieldName.ANTLERS_INNER_WIDTH);
        }

        specimen.setAntlersInnerWidth(50);
        test(HarvestSpecimenValidator::validateAntlersInnerWidth);
    }

    @Theory
    public void testValidateAntlersInnerWidth_whenMissing(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                          @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                          @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                          @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                          @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        test(HarvestSpecimenValidator::validateAntlersInnerWidth);
    }

    @Theory
    public void testValidateAntlerShaftWidth_whenGiven(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                       @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                       @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                       @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                       @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        if (!specVersion.supportsAntlerFields2020()
                || huntingYear < 2020
                || !isRoeDeer()
                || !specimenType.isAdultMaleAndAntlersPresent()) {

            expectIllegal(HarvestSpecimenFieldName.ANTLER_SHAFT_WIDTH);
        }

        specimen.setAntlerShaftWidth(50);
        test(HarvestSpecimenValidator::validateAntlerShaftWidth);
    }

    @Theory
    public void testValidateAntlerShaftWidth_whenMissing(@FromDataPoints("huntingYear") final Integer huntingYear,
                                                         @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                                         @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                                         @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                                         @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        test(HarvestSpecimenValidator::validateAntlerShaftWidth);
    }

    @Theory
    public void testValidateAlone_whenGiven(@FromDataPoints("huntingYear") final Integer huntingYear,
                                            @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                            @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                            @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                            @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        if (!isMoose() || !specimenType.isYoung()) {
            expectIllegal(HarvestSpecimenFieldName.ALONE);
        }

        specimen.setAlone(someBoolean());
        test(HarvestSpecimenValidator::validateAlone);
    }

    @Theory
    public void testValidateAlone_whenMissing(@FromDataPoints("huntingYear") final Integer huntingYear,
                                              @FromDataPoints("gameSpeciesCode") final Integer gameSpeciesCode,
                                              @FromDataPoints("specimenType") final HarvestSpecimenType specimenType,
                                              @FromDataPoints("linkedToHuntingDay") final Boolean linkedToHuntingDay,
                                              @FromDataPoints("specVersion") final HarvestSpecVersion specVersion) {
        setup(huntingYear, gameSpeciesCode, specimenType, linkedToHuntingDay, specVersion);
        test(HarvestSpecimenValidator::validateAlone);
    }

    private void expectMissing(final HarvestSpecimenFieldName fieldName) {
        // adds expectation that current test will throw an error
        // this is wanted behavior when test data/parameters are in invalid state
        thrown.expectMessage("missing " + fieldName.name());
    }

    private void expectIllegal(final HarvestSpecimenFieldName fieldName) {
        // adds expectation that current test will throw an error
        // this is wanted behavior when test data/parameters are in invalid state
        thrown.expectMessage("illegal " + fieldName.name());
    }

    private void expectInvalid(final HarvestSpecimenFieldName fieldName, final Object value) {
        // adds expectation that current test will throw an error
        // this is wanted behavior when test data/parameters are in invalid state
        thrown.expectMessage("invalid " + fieldName.name() + ": " + value);
    }

}
