package fi.riista.feature.gamediary.harvest.fields;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.stream.IntStream;

import static fi.riista.feature.gamediary.GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static fi.riista.feature.gamediary.GameSpecies.isWhiteTailedDeer;
import static fi.riista.util.DateUtil.huntingYear;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class LegallyMandatoryFieldsMooselikeTest {

    @DataPoints("huntingYears")
    public static final int[] HUNTING_YEAR_DATA_POINT = IntStream.rangeClosed(2014, huntingYear()).toArray();

    @DataPoints("speciesCodes")
    public static final ImmutableSet<Integer> GAME_SPECIES_CODES = MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING;

    @Theory
    public void testPermitHarvest_formFields(@FromDataPoints("speciesCodes") final int speciesCode) {
        assumeFalse(isWhiteTailedDeer(speciesCode));

        final RequiredHarvestFields.Report formFields =
                LegallyMandatoryFieldsMooselike.getFormFields(speciesCode);

        assertEquals(RequiredHarvestField.NO, formFields.getHarvestArea());
        assertEquals(RequiredHarvestField.NO, formFields.getFeedingPlace());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingMethod());
        assertEquals(RequiredHarvestField.NO, formFields.getLukeStatus());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaType());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaSize());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingParty());
        assertEquals(RequiredHarvestField.NO, formFields.getReportedWithPhoneCall());
        assertEquals(RequiredHarvestField.NO, formFields.getDeerHuntingType());
    }

    @Test
    public void testPermitHarvest_formFields_whitTailedDeer() {

        final RequiredHarvestFields.Report formFields =
                LegallyMandatoryFieldsMooselike.getFormFields(OFFICIAL_CODE_WHITE_TAILED_DEER);

        assertEquals(RequiredHarvestField.NO, formFields.getHarvestArea());
        assertEquals(RequiredHarvestField.NO, formFields.getFeedingPlace());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingMethod());
        assertEquals(RequiredHarvestField.NO, formFields.getLukeStatus());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaType());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingAreaSize());
        assertEquals(RequiredHarvestField.NO, formFields.getHuntingParty());
        assertEquals(RequiredHarvestField.NO, formFields.getReportedWithPhoneCall());
        assertEquals(RequiredHarvestField.VOLUNTARY, formFields.getDeerHuntingType());
    }

    @Theory
    public void testSpecimenFields_antlers_moose(@FromDataPoints("huntingYears") final int huntingYear,
                                                 final boolean isClientSupportFor2020Fields) {

        final LegallyMandatoryFieldsMooselike.Specimen specimenFields =
                getSpecimenFields(huntingYear, OFFICIAL_CODE_MOOSE, isClientSupportFor2020Fields);

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

    @Theory
    public void testSpecimenFields_antlers_whiteTailedDeer(@FromDataPoints("huntingYears") final int huntingYear,
                                                           final boolean isClientSupportFor2020Fields) {

        final LegallyMandatoryFieldsMooselike.Specimen specimenFields =
                getSpecimenFields(huntingYear, OFFICIAL_CODE_WHITE_TAILED_DEER, isClientSupportFor2020Fields);

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

    @Theory
    public void testSpecimenFields_antlers_fallowDeer(@FromDataPoints("huntingYears") final int huntingYear,
                                                      final boolean isClientSupportFor2020Fields) {

        final LegallyMandatoryFieldsMooselike.Specimen specimenFields =
                getSpecimenFields(huntingYear, OFFICIAL_CODE_FALLOW_DEER, isClientSupportFor2020Fields);

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

    @Theory
    public void testSpecimenFields_antlers_wildForestReindeer(@FromDataPoints("huntingYears") final int huntingYear,
                                                              final boolean isClientSupportFor2020Fields) {

        final LegallyMandatoryFieldsMooselike.Specimen specimenFields =
                getSpecimenFields(huntingYear, OFFICIAL_CODE_WILD_FOREST_REINDEER, isClientSupportFor2020Fields);

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

    @Theory
    public void testSpecimenFields_fitnessClass_moose(@FromDataPoints("huntingYears") final int huntingYear,
                                                      final boolean isClientSupportFor2020Fields) {

        final LegallyMandatoryFieldsMooselike.Specimen specimenFields =
                getSpecimenFields(huntingYear, OFFICIAL_CODE_MOOSE, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getFitnessClass());
    }

    @Theory
    public void testSpecimenFields_fitnessClass_otherSpecies(@FromDataPoints("huntingYears") final int huntingYear,
                                                             @FromDataPoints("speciesCodes") final int speciesCode,
                                                             final boolean isClientSupportFor2020Fields) {
        assumeTrue(speciesCode != OFFICIAL_CODE_MOOSE);

        final LegallyMandatoryFieldsMooselike.Specimen specimenFields =
                getSpecimenFields(huntingYear, speciesCode, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getFitnessClass());
    }

    @Theory
    public void testSpecimenFields_alone_moose(@FromDataPoints("huntingYears") final int huntingYear,
                                               final boolean isClientSupportFor2020Fields) {

        final LegallyMandatoryFieldsMooselike.Specimen specimenFields =
                getSpecimenFields(huntingYear, OFFICIAL_CODE_MOOSE, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY_IF_YOUNG, specimenFields.getAlone());
    }

    @Theory
    public void testSpecimenFields_alone_otherSpecies(@FromDataPoints("huntingYears") final int huntingYear,
                                                      @FromDataPoints("speciesCodes") final int speciesCode,
                                                      final boolean isClientSupportFor2020Fields) {

        assumeTrue(speciesCode != OFFICIAL_CODE_MOOSE);

        final LegallyMandatoryFieldsMooselike.Specimen specimenFields =
                getSpecimenFields(huntingYear, speciesCode, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.NO, specimenFields.getAlone());
    }

    @Theory
    public void testSpecimenFields_otherSpecimenFields(@FromDataPoints("huntingYears") final int huntingYear,
                                                       @FromDataPoints("speciesCodes") final int speciesCode,
                                                       final boolean isClientSupportFor2020Fields) {

        final LegallyMandatoryFieldsMooselike.Specimen specimenFields =
                getSpecimenFields(huntingYear, speciesCode, isClientSupportFor2020Fields);

        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getAge());
        assertEquals(RequiredHarvestSpecimenField.YES, specimenFields.getGender());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getAdditionalInfo());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getNotEdible());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightEstimated());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeightMeasured());
        assertEquals(RequiredHarvestSpecimenField.VOLUNTARY, specimenFields.getWeight());
    }

    private static LegallyMandatoryFieldsMooselike.Specimen getSpecimenFields(final int huntingYear,
                                                                              final int speciesCode,
                                                                              final boolean isClientSupportFor2020Fields) {

        return LegallyMandatoryFieldsMooselike.getSpecimenFields(huntingYear, speciesCode, isClientSupportFor2020Fields);
    }
}
