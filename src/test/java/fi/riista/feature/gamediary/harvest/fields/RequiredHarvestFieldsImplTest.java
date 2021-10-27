package fi.riista.feature.gamediary.harvest.fields;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.HuntingMethod;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.stream.IntStream;

import static fi.riista.feature.gamediary.GameSpecies.ALL_GAME_SPECIES_CODES;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GREY_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.gamediary.harvest.HarvestReportingType.BASIC;
import static fi.riista.feature.gamediary.harvest.HarvestReportingType.HUNTING_DAY;
import static fi.riista.feature.gamediary.harvest.HarvestReportingType.PERMIT;
import static fi.riista.feature.gamediary.harvest.HarvestReportingType.SEASON;
import static fi.riista.feature.gamediary.harvest.HuntingMethod.SHOT_BUT_LOST;
import static fi.riista.util.DateUtil.huntingYear;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class RequiredHarvestFieldsImplTest {

    @DataPoints("huntingYears")
    public static final int[] HUNTING_YEAR_DATA_POINT = IntStream.rangeClosed(2014, huntingYear()).toArray();

    @DataPoints("speciesCodes")
    public static final int[] SPECIES_DATA_POINT = ALL_GAME_SPECIES_CODES;

    private static void assertNoMooselikeFields(final RequiredHarvestFields.Specimen specimenFields) {
        assertNoMooselikeWeightFields(specimenFields);

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getFitnessClass());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getNotEdible());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAdditionalInfo());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAlone());

        assertNoAntlerFields(specimenFields);
    }

    private static void assertNoMooselikeFieldsExceptWeightEstimated(final RequiredHarvestFields.Specimen specimenFields) {
        assertEquals(RequiredHarvestSpecimenField.ALLOWED_BUT_HIDDEN, specimenFields.getWeightEstimated());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeightMeasured());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getFitnessClass());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getNotEdible());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAdditionalInfo());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAlone());

        assertNoAntlerFields(specimenFields);
    }

    private static void assertNoMooselikeWeightFields(final RequiredHarvestFields.Specimen specimenFields) {
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeightEstimated());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeightMeasured());
    }

    private static void assertNoAntlerFields(final RequiredHarvestFields.Specimen specimenFields) {
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLost());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersType());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersWidth());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlerPoints());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersGirth());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLength());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersInnerWidth());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlerShaftWidth());
    }

    // BASIC DIARY ENTRY

    @Theory
    public void testBasicDiaryEntry_commonSpecimenFields_mooselike(@FromDataPoints("huntingYears") final int huntingYear,
                                                                   @FromDataPoints("speciesCodes") final int speciesCode,
                                                                   final boolean isClientSupportFor2020Fields) {
        assumeTrue(GameSpecies.isMooselike(speciesCode));

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, speciesCode, null, BASIC, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getGender());

        if (GameSpecies.isRoeDeer(speciesCode)) {
            if (huntingYear >= 2020 && isClientSupportFor2020Fields) {
                assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeight());
            } else {
                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeight());
            }
        } else if (huntingYear >= 2016) {
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeight());
        } else {
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeight());
        }
        
        // Additional testing is done in HarvestSpecimenValidatorTest
    }

    @Theory
    public void testBasicDiaryEntry_commonSpecimenFields_otherSpecies(@FromDataPoints("huntingYears") final int huntingYear,
                                                                      @FromDataPoints("speciesCodes") final int speciesCode,
                                                                      final boolean isClientSupportFor2020Fields) {

        assumeFalse(GameSpecies.isMooselike(speciesCode) || GameSpecies.isWildBoar(speciesCode));

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, speciesCode, null, BASIC, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getGender());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeight());

        assertNoMooselikeFields(specimenFields);
    }

    @Theory
    public void testBasicDiaryEntry_formFields(@FromDataPoints("huntingYears") final int huntingYear,
                                               @FromDataPoints("speciesCodes") final int speciesCode) {

        final RequiredHarvestFields.Report formFields =
                RequiredHarvestFieldsImpl.getFormFields(huntingYear, speciesCode, BASIC);

        assertEquals(RequiredHarvestField.NO, formFields.getPermitNumber());
        assertEquals(RequiredHarvestField.NO, formFields.getFeedingPlace());
        assertEquals(RequiredHarvestField.NO, formFields.getHarvestArea());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingMethod());
        assertEquals(RequiredHarvestField.NO, formFields.getLukeStatus());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaType());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaSize());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingParty());
        assertEquals(RequiredHarvestField.NO, formFields.getReportedWithPhoneCall());

        final RequiredHarvestField expectedDeerHuntingType =
                huntingYear >= 2020 && GameSpecies.isWhiteTailedDeer(speciesCode)
                        ? RequiredHarvestField.VOLUNTARY
                        : RequiredHarvestField.NO;

        assertEquals(expectedDeerHuntingType, formFields.getDeerHuntingType());
    }

    // MOOSE

    @Theory
    public void testMoose_basic_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                               final boolean isClientSupportFor2020Fields) {
        assumeTrue(huntingYear >= 2016);

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, OFFICIAL_CODE_MOOSE, null, BASIC, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getGender());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeight());

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightEstimated());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightMeasured());

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getFitnessClass());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getNotEdible());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAdditionalInfo());

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_YOUNG, specimenFields.getAlone());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLength());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersInnerWidth());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlerShaftWidth());

        if (isClientSupportFor2020Fields && huntingYear >= 2020) {
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersType());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlerPoints());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersGirth());
        } else {
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlersType());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlerPoints());
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersGirth());
        }
    }

    @Theory
    public void testMoose_huntingDay_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                                    final boolean isClientSupportFor2020Fields) {
        assumeTrue(huntingYear >= 2016);

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, OFFICIAL_CODE_MOOSE, null, HUNTING_DAY, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getGender());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeight());

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightEstimated());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightMeasured());

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getFitnessClass());
        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getNotEdible());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAdditionalInfo());

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_YOUNG, specimenFields.getAlone());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLength());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersInnerWidth());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlerShaftWidth());

        if (isClientSupportFor2020Fields && huntingYear >= 2020) {
            assertEquals(RequiredHarvestSpecimenField.YES_IF_ADULT_MALE, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersType());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlerPoints());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersGirth());
        } else {
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlersType());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlerPoints());
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersGirth());
        }
    }

    // FALLOW DEER

    @Theory
    public void testFallowDeer_basic_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                                    final boolean isClientSupportFor2020Fields) {
        assumeTrue(huntingYear >= 2016);

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, OFFICIAL_CODE_FALLOW_DEER, null, BASIC, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getGender());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeight());

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightEstimated());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightMeasured());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getFitnessClass());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getNotEdible());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAdditionalInfo());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAlone());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersType());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersGirth());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLength());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersInnerWidth());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlerShaftWidth());

        if (isClientSupportFor2020Fields && huntingYear >= 2020) {
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlerPoints());
        } else {
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlerPoints());
        }
    }

    @Theory
    public void testFallowDeer_huntingDay_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                                         final boolean isClientSupportFor2020Fields) {
        assumeTrue(huntingYear >= 2016);

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, OFFICIAL_CODE_FALLOW_DEER, null, HUNTING_DAY, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getGender());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeight());

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightEstimated());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightMeasured());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getFitnessClass());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getNotEdible());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAdditionalInfo());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAlone());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersType());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersGirth());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLength());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersInnerWidth());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlerShaftWidth());

        if (isClientSupportFor2020Fields && huntingYear >= 2020) {
            assertEquals(RequiredHarvestSpecimenField.YES_IF_ADULT_MALE, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlerPoints());
        } else {
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlerPoints());
        }
    }

    // WHITE-TAILED DEER

    @Theory
    public void testWhiteTailedDeer_basic_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                                         final boolean isClientSupportFor2020Fields) {
        assumeTrue(huntingYear >= 2016);

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, OFFICIAL_CODE_WHITE_TAILED_DEER, null, BASIC, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getGender());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeight());

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightEstimated());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightMeasured());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getFitnessClass());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getNotEdible());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAdditionalInfo());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAlone());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersType());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlerShaftWidth());

        if (isClientSupportFor2020Fields && huntingYear >= 2020) {
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.DEPRECATED_ANTLER_DETAIL, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlerPoints());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersGirth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersLength());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersInnerWidth());
        } else {
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlerPoints());
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersGirth());
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLength());
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersInnerWidth());
        }
    }

    @Theory
    public void testWhiteTailedDeer_huntingDay_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                                              final boolean isClientSupportFor2020Fields) {
        assumeTrue(huntingYear >= 2016);

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, OFFICIAL_CODE_WHITE_TAILED_DEER, null, HUNTING_DAY, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getGender());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeight());

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightEstimated());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightMeasured());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getFitnessClass());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getNotEdible());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAdditionalInfo());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAlone());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersType());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlerShaftWidth());

        if (isClientSupportFor2020Fields && huntingYear >= 2020) {
            assertEquals(RequiredHarvestSpecimenField.YES_IF_ADULT_MALE, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.DEPRECATED_ANTLER_DETAIL, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlerPoints());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersGirth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersLength());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersInnerWidth());
        } else {
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlerPoints());
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersGirth());
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLength());
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersInnerWidth());
        }
    }

    // WILD FOREST REINDEER

    @Theory
    public void testWildForestReindeer_basic_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                                            final boolean isClientSupportFor2020Fields) {
        assumeTrue(huntingYear >= 2016);

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, OFFICIAL_CODE_WILD_FOREST_REINDEER, null, BASIC, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getGender());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeight());

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightEstimated());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightMeasured());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getFitnessClass());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getNotEdible());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAdditionalInfo());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAlone());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersType());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersGirth());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLength());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersInnerWidth());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlerShaftWidth());

        if (isClientSupportFor2020Fields && huntingYear >= 2020) {
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlerPoints());
        } else {
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlerPoints());
        }
    }

    @Theory
    public void testWildForestReindeer_huntingDay_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                                                 final boolean isClientSupportFor2020Fields) {
        assumeTrue(huntingYear >= 2016);

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, OFFICIAL_CODE_WILD_FOREST_REINDEER, null, HUNTING_DAY, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getGender());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeight());

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightEstimated());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightMeasured());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getFitnessClass());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getNotEdible());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAdditionalInfo());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAlone());

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersType());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersGirth());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLength());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersInnerWidth());
        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlerShaftWidth());

        if (isClientSupportFor2020Fields && huntingYear >= 2020) {
            assertEquals(RequiredHarvestSpecimenField.YES_IF_ADULT_MALE, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlerPoints());
        } else {
            assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersLost());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlersWidth());
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlerPoints());
        }
    }

    // SPECIAL PERMITS

    @Theory
    public void testPermitHarvest_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                                 @FromDataPoints("speciesCodes") final int speciesCode,
                                                 final boolean isClientSupportFor2020Fields) {

        // If true, testing would only be done for HarvestSpecimenValidator.
        assumeFalse(GameSpecies.isMooselike(speciesCode) || GameSpecies.isWildBoar(speciesCode));

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, speciesCode, null, PERMIT, isClientSupportFor2020Fields);

        if (RequiredHarvestFieldsImpl.SpecimenImpl.PERMIT_MANDATORY_AGE.contains(speciesCode)) {
            assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getAge());
        } else {
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAge());
        }

        if (RequiredHarvestFieldsImpl.SpecimenImpl.PERMIT_MANDATORY_GENDER.contains(speciesCode)) {
            assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getGender());
        } else {
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getGender());
        }

        if (RequiredHarvestFieldsImpl.SpecimenImpl.PERMIT_MANDATORY_WEIGHT.contains(speciesCode)) {
            assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getWeight());
        } else {
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeight());
        }

        assertNoMooselikeFields(specimenFields);
    }

    @Theory
    public void testPermitHarvest_formFields(@FromDataPoints("huntingYears") final int huntingYear,
                                             @FromDataPoints("speciesCodes") final int speciesCode) {

        final RequiredHarvestFields.Report formFields =
                RequiredHarvestFieldsImpl.getFormFields(huntingYear, speciesCode, PERMIT);

        final RequiredHarvestField expectedFeedingPlace =
                GameSpecies.isWildBoar(speciesCode) ? RequiredHarvestField.VOLUNTARY : RequiredHarvestField.NO;

        final RequiredHarvestField expectedHuntingMethod =
                GameSpecies.isGreySeal(speciesCode) ? RequiredHarvestField.YES : RequiredHarvestField.NO;

        assertEquals(expectedFeedingPlace, formFields.getFeedingPlace());
        assertEquals(expectedHuntingMethod, formFields.getHuntingMethod());

        assertEquals(RequiredHarvestField.NO, formFields.getHarvestArea());
        assertEquals(RequiredHarvestField.NO, formFields.getLukeStatus());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaType());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaSize());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingParty());
        assertEquals(RequiredHarvestField.NO, formFields.getReportedWithPhoneCall());
        assertEquals(RequiredHarvestField.NO, formFields.getDeerHuntingType());
    }

    // BEAR

    @Theory
    public void testBear_season_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                               final boolean isClientSupportFor2020Fields) {

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, OFFICIAL_CODE_BEAR, null, SEASON, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getGender());
        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getWeight());

        assertNoMooselikeFields(specimenFields);
    }

    @Theory
    public void testBear_season_formFields(@FromDataPoints("huntingYears") final int huntingYear) {

        final RequiredHarvestFields.Report formFields =
                RequiredHarvestFieldsImpl.getFormFields(huntingYear, OFFICIAL_CODE_BEAR, SEASON);

        assertEquals(RequiredHarvestField.NO, formFields.getFeedingPlace());
        assertEquals(RequiredHarvestField.YES, formFields.getHarvestArea());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingMethod());
        assertEquals(RequiredHarvestField.NO, formFields.getLukeStatus());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaType());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaSize());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingParty());
        assertEquals(RequiredHarvestField.NO, formFields.getDeerHuntingType());

        final RequiredHarvestField expectedReportedWithPhoneCall =
                huntingYear >= 2015 ? RequiredHarvestField.NO : RequiredHarvestField.YES;

        assertEquals(expectedReportedWithPhoneCall, formFields.getReportedWithPhoneCall());
    }

    // GREY SEAL

    @Theory
    public void testGreySealSeason_season_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                                         final HuntingMethod huntingMethod,
                                                         final boolean isClientSupportFor2020Fields) {

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, OFFICIAL_CODE_GREY_SEAL, huntingMethod, SEASON, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getGender());

        RequiredHarvestSpecimenField expectedWeight = RequiredHarvestSpecimenField.YES;

        if (huntingMethod == SHOT_BUT_LOST) {
            expectedWeight =
                    huntingYear >= 2015 ? RequiredHarvestSpecimenField.NO : RequiredHarvestSpecimenField.VOLUNTARY;
        }

        assertEquals(expectedWeight, specimenFields.getWeight());
    }

    @Theory
    public void testGreySealSeason_season_formFields(@FromDataPoints("huntingYears") final int huntingYear) {

        final RequiredHarvestFields.Report formFields = RequiredHarvestFieldsImpl
                .getFormFields(huntingYear, OFFICIAL_CODE_GREY_SEAL, SEASON);

        assertEquals(RequiredHarvestField.NO, formFields.getFeedingPlace());
        assertEquals(RequiredHarvestField.YES, formFields.getHarvestArea());
        assertEquals(RequiredHarvestField.YES, formFields.getHuntingMethod());
        assertEquals(RequiredHarvestField.NO, formFields.getLukeStatus());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaType());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaSize());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingParty());
        assertEquals(RequiredHarvestField.NO, formFields.getReportedWithPhoneCall());
        assertEquals(RequiredHarvestField.NO, formFields.getDeerHuntingType());
    }

    // ROE DEER

    @Theory
    public void testRoeDeer_season_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                                  final boolean isClientSupportFor2020Fields) {

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, OFFICIAL_CODE_ROE_DEER, null, SEASON, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getGender());

        if (huntingYear >= 2020) {
            if (isClientSupportFor2020Fields) {
                assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeight());

                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightEstimated());
                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightMeasured());

                assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getFitnessClass());
                assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getNotEdible());
                assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAdditionalInfo());

                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ADULT_MALE, specimenFields.getAntlersLost());
                assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersType());
                assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersWidth());
                assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersGirth());
                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlerPoints());
                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlersLength());
                assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAntlersInnerWidth());
                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_ANTLERS_PRESENT, specimenFields.getAntlerShaftWidth());
            } else {
                assertNoMooselikeFieldsExceptWeightEstimated(specimenFields);
            }
        } else {
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeight());
            assertNoMooselikeFields(specimenFields);
        }
    }

    @Theory
    public void testRoeDeer_season_formFields_startingFrom2017(@FromDataPoints("huntingYears") final int huntingYear) {
        assumeTrue(huntingYear >= 2017);

        final RequiredHarvestFields.Report formFields = RequiredHarvestFieldsImpl
                .getFormFields(huntingYear, OFFICIAL_CODE_ROE_DEER, SEASON);

        assertEquals(RequiredHarvestField.NO, formFields.getFeedingPlace());
        assertEquals(RequiredHarvestField.NO, formFields.getHarvestArea());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingMethod());
        assertEquals(RequiredHarvestField.NO, formFields.getLukeStatus());
        assertEquals(RequiredHarvestField.VOLUNTARY, formFields.getHuntingAreaType());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaSize());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingParty());
        assertEquals(RequiredHarvestField.NO, formFields.getReportedWithPhoneCall());
        assertEquals(RequiredHarvestField.NO, formFields.getDeerHuntingType());
    }

    @Theory
    public void testRoeDeer_season_formFields_before2017(@FromDataPoints("huntingYears") final int huntingYear) {
        assumeTrue(huntingYear < 2017);

        final RequiredHarvestFields.Report formFields = RequiredHarvestFieldsImpl
                .getFormFields(huntingYear, OFFICIAL_CODE_ROE_DEER, SEASON);

        assertEquals(RequiredHarvestField.NO, formFields.getFeedingPlace());
        assertEquals(RequiredHarvestField.NO, formFields.getHarvestArea());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingMethod());
        assertEquals(RequiredHarvestField.NO, formFields.getLukeStatus());
        assertEquals(RequiredHarvestField.YES, formFields.getHuntingAreaType());
        assertEquals(RequiredHarvestField.YES, formFields.getHuntingAreaSize());
        assertEquals(RequiredHarvestField.VOLUNTARY, formFields.getHuntingParty());
        assertEquals(RequiredHarvestField.NO, formFields.getReportedWithPhoneCall());
        assertEquals(RequiredHarvestField.NO, formFields.getDeerHuntingType());
    }

    // WOLF

    @Theory
    public void testWolf_season_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                               final boolean isClientSupportFor2020Fields) {

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, OFFICIAL_CODE_WOLF, null, SEASON, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getGender());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeight());

        assertNoMooselikeFields(specimenFields);
    }

    @Theory
    public void testWolf_season_formFields(@FromDataPoints("huntingYears") final int huntingYear) {

        final RequiredHarvestFields.Report formFields =
                RequiredHarvestFieldsImpl.getFormFields(huntingYear, OFFICIAL_CODE_WOLF, SEASON);

        assertEquals(RequiredHarvestField.NO, formFields.getFeedingPlace());
        assertEquals(RequiredHarvestField.NO, formFields.getHarvestArea());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingMethod());
        assertEquals(RequiredHarvestField.VOLUNTARY, formFields.getLukeStatus());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaType());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaSize());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingParty());
        assertEquals(RequiredHarvestField.NO, formFields.getReportedWithPhoneCall());
        assertEquals(RequiredHarvestField.NO, formFields.getDeerHuntingType());
    }

    // WILD BOAR

    @Theory
    public void testWildBoar_basic_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                                  final boolean isClientSupportFor2020Fields) {

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, OFFICIAL_CODE_WILD_BOAR, null, BASIC, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getGender());

        if (huntingYear >= 2020) {
            if (isClientSupportFor2020Fields) {
                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightEstimated());
                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightMeasured());
                assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeight());
            } else {
                assertNoMooselikeFieldsExceptWeightEstimated(specimenFields);
                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeight());
            }
        } else {
            assertNoMooselikeFields(specimenFields);
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeight());
        }
    }

    @Theory
    public void testWildBoar_permit_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                                   final boolean isClientSupportFor2020Fields) {

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, OFFICIAL_CODE_WILD_BOAR, null, PERMIT, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getGender());

        if (huntingYear >= 2020) {
            if (isClientSupportFor2020Fields) {
                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightEstimated());
                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightMeasured());
                assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getWeight());
            } else {
                assertNoMooselikeFieldsExceptWeightEstimated(specimenFields);
                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeight());
            }
        } else {
            assertNoMooselikeFields(specimenFields);
            assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeight());
        }
    }

    @Theory
    public void testWildBoar_season_specimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                                   final boolean isClientSupportFor2020Fields) {

        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFieldsImpl
                .getSpecimenFields(huntingYear, OFFICIAL_CODE_WILD_BOAR, null, SEASON, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getGender());

        if (huntingYear >= 2020) {
            if (isClientSupportFor2020Fields) {
                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightEstimated());
                assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightMeasured());
            } else {
                assertNoMooselikeFieldsExceptWeightEstimated(specimenFields);
            }
        } else {
            assertNoMooselikeFields(specimenFields);
        }
    }

    @Theory
    public void testWildBoar_season_formFields(@FromDataPoints("huntingYears") final int huntingYear) {

        final RequiredHarvestFields.Report formFields = RequiredHarvestFieldsImpl
                .getFormFields(huntingYear, OFFICIAL_CODE_WILD_BOAR, SEASON);

        assertEquals(RequiredHarvestField.VOLUNTARY, formFields.getFeedingPlace());
        assertEquals(RequiredHarvestField.NO, formFields.getHarvestArea());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingMethod());
        assertEquals(RequiredHarvestField.NO, formFields.getLukeStatus());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaType());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaSize());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingParty());
        assertEquals(RequiredHarvestField.NO, formFields.getReportedWithPhoneCall());
        assertEquals(RequiredHarvestField.NO, formFields.getDeerHuntingType());
    }
}
