package fi.riista.feature.gamediary.harvest.fields;

import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HuntingMethod;
import fi.riista.util.DateUtil;
import org.junit.Test;

import static fi.riista.feature.gamediary.GameSpecies.ALL_GAME_SPECIES_CODES;
import static org.junit.Assert.assertEquals;

public class RequiredHarvestFieldsTest {

    private static final int HUNTING_YEAR = 2017;

    private static void assertNoMooseFields(RequiredHarvestFields.Specimen specimenFields) {
        assertEquals(Required.NO, specimenFields.getAdditionalInfo());
        assertEquals(Required.NO, specimenFields.getNotEdible());
        assertEquals(Required.NO, specimenFields.getFitnessClass());
        assertEquals(Required.NO, specimenFields.getWeightEstimated());
        assertEquals(Required.NO, specimenFields.getWeightMeasured());

        for (GameAge age : GameAge.values()) {
            assertEquals(Required.NO, specimenFields.getAlone(age));

            for (GameGender gender : GameGender.values()) {
                assertEquals(Required.NO, specimenFields.getAntlersType(age, gender));
                assertEquals(Required.NO, specimenFields.getAntlersWidth(age, gender));
                assertEquals(Required.NO, specimenFields.getAntlerPoints(age, gender));
            }
        }
    }

    // BASIC DIARY ENTRY

    @Test
    public void testBasicDiaryEntry_SpecimenFields() {
        for (int gameSpeciesCode : ALL_GAME_SPECIES_CODES) {
            final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFields.getSpecimenFields(
                    HUNTING_YEAR, gameSpeciesCode, null, HarvestReportingType.BASIC);

            assertEquals(Required.VOLUNTARY, specimenFields.getAge());
            assertEquals(Required.VOLUNTARY, specimenFields.getGender());

            if (GameSpecies.isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode)) {
                // Additional testing is done using HarvestSpecimenValidatorTest
                assertEquals(Required.NO, specimenFields.getWeight());

            } else {
                assertEquals(Required.VOLUNTARY, specimenFields.getWeight());

                assertNoMooseFields(specimenFields);
            }
        }
    }

    @Test
    public void testBasicDiaryEntry_FormFields() {
        for (int gameSpeciesCode : ALL_GAME_SPECIES_CODES) {
            final RequiredHarvestFields.Report formFields = RequiredHarvestFields.getFormFields(
                    HUNTING_YEAR, gameSpeciesCode, HarvestReportingType.BASIC);

            assertEquals(Required.NO, formFields.getPermitNumber());
            assertEquals(Required.NO, formFields.getFeedingPlace());
            assertEquals(Required.NO, formFields.getHarvestArea());
            assertEquals(Required.NO, formFields.getHuntingMethod());
            assertEquals(Required.NO, formFields.getLukeStatus());
            assertEquals(Required.NO, formFields.getHuntingAreaType());
            assertEquals(Required.NO, formFields.getHuntingAreaSize());
            assertEquals(Required.NO, formFields.getHuntingParty());
            assertEquals(Required.NO, formFields.getReportedWithPhoneCall());
        }
    }

    @Test
    public void testMoose_Basic_SpecimenFields() {
        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFields.getSpecimenFields(
                HUNTING_YEAR, GameSpecies.OFFICIAL_CODE_MOOSE, null, HarvestReportingType.BASIC);

        assertEquals(Required.NO, specimenFields.getWeight());
        assertEquals(Required.VOLUNTARY, specimenFields.getAge());
        assertEquals(Required.VOLUNTARY, specimenFields.getGender());

        assertEquals(Required.VOLUNTARY, specimenFields.getAdditionalInfo());
        assertEquals(Required.VOLUNTARY, specimenFields.getNotEdible());
        assertEquals(Required.VOLUNTARY, specimenFields.getFitnessClass());
        assertEquals(Required.VOLUNTARY, specimenFields.getWeightEstimated());
        assertEquals(Required.VOLUNTARY, specimenFields.getWeightMeasured());

        for (GameAge age : GameAge.values()) {
            if (age == GameAge.YOUNG) {
                assertEquals(Required.VOLUNTARY, specimenFields.getAlone(age));
            } else {
                assertEquals(Required.NO, specimenFields.getAlone(age));
            }

            for (GameGender gender : GameGender.values()) {
                if (age == GameAge.ADULT && gender == GameGender.MALE) {
                    assertEquals(Required.VOLUNTARY, specimenFields.getAntlersType(age, gender));
                    assertEquals(Required.VOLUNTARY, specimenFields.getAntlersWidth(age, gender));
                    assertEquals(Required.VOLUNTARY, specimenFields.getAntlerPoints(age, gender));
                } else {
                    assertEquals(Required.NO, specimenFields.getAntlersType(age, gender));
                    assertEquals(Required.NO, specimenFields.getAntlersWidth(age, gender));
                    assertEquals(Required.NO, specimenFields.getAntlerPoints(age, gender));
                }
            }
        }
    }

    @Test
    public void testWhiteTailedDeer_Basic_SpecimenFields() {
        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFields.getSpecimenFields(
                HUNTING_YEAR, GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER, null, HarvestReportingType.BASIC);

        assertEquals(Required.NO, specimenFields.getWeight());
        assertEquals(Required.VOLUNTARY, specimenFields.getAge());
        assertEquals(Required.VOLUNTARY, specimenFields.getGender());

        assertEquals(Required.VOLUNTARY, specimenFields.getAdditionalInfo());
        assertEquals(Required.VOLUNTARY, specimenFields.getNotEdible());
        assertEquals(Required.NO, specimenFields.getFitnessClass());
        assertEquals(Required.VOLUNTARY, specimenFields.getWeightEstimated());
        assertEquals(Required.VOLUNTARY, specimenFields.getWeightMeasured());

        for (GameAge age : GameAge.values()) {
            assertEquals(Required.NO, specimenFields.getAlone(age));

            for (GameGender gender : GameGender.values()) {
                assertEquals(Required.NO, specimenFields.getAntlersType(age, gender));

                if (age == GameAge.ADULT && gender == GameGender.MALE) {
                    assertEquals(Required.VOLUNTARY, specimenFields.getAntlersWidth(age, gender));
                    assertEquals(Required.VOLUNTARY, specimenFields.getAntlerPoints(age, gender));
                } else {
                    assertEquals(Required.NO, specimenFields.getAntlersWidth(age, gender));
                    assertEquals(Required.NO, specimenFields.getAntlerPoints(age, gender));
                }
            }
        }
    }

    // SPECIAL PERMITS

    @Test
    public void testPermitHarvest_SpecimenFields() {
        for (int gameSpeciesCode : ALL_GAME_SPECIES_CODES) {
            if (GameSpecies.isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode)) {
                // Testing is done only for HarvestSpecimenValidator
                continue;
            }

            final int huntingYear = DateUtil.huntingYear();
            final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFields.getSpecimenFields(
                    huntingYear, gameSpeciesCode, null, HarvestReportingType.PERMIT);

            if (RequiredHarvestFields.Specimen.PERMIT_MANDATORY_AGE.contains(gameSpeciesCode)) {
                assertEquals(Required.YES, specimenFields.getAge());
            } else {
                assertEquals(Required.VOLUNTARY, specimenFields.getAge());
            }

            if (RequiredHarvestFields.Specimen.PERMIT_MANDATORY_GENDER.contains(gameSpeciesCode)) {
                assertEquals(Required.YES, specimenFields.getGender());
            } else {
                assertEquals(Required.VOLUNTARY, specimenFields.getGender());
            }

            if (RequiredHarvestFields.Specimen.PERMIT_MANDATORY_WEIGHT.contains(gameSpeciesCode)) {
                assertEquals(Required.YES, specimenFields.getWeight());
            } else {
                assertEquals(Required.VOLUNTARY, specimenFields.getWeight());
            }

            assertNoMooseFields(specimenFields);
        }
    }

    @Test
    public void testPermitHarvest_FormFields() {
        for (int gameSpeciesCode : ALL_GAME_SPECIES_CODES) {
            final RequiredHarvestFields.Report formFields = RequiredHarvestFields.getFormFields(
                    HUNTING_YEAR, gameSpeciesCode, HarvestReportingType.PERMIT);

            assertEquals(Required.NO, formFields.getHarvestArea());

            if (gameSpeciesCode == GameSpecies.OFFICIAL_CODE_WILD_BOAR) {
                assertEquals(Required.VOLUNTARY, formFields.getFeedingPlace());
            } else {
                assertEquals(Required.NO, formFields.getFeedingPlace());
            }

            if (gameSpeciesCode == GameSpecies.OFFICIAL_CODE_GREY_SEAL) {
                assertEquals(Required.YES, formFields.getHuntingMethod());
            } else {
                assertEquals(Required.NO, formFields.getHuntingMethod());
            }

            assertEquals(Required.NO, formFields.getLukeStatus());
            assertEquals(Required.NO, formFields.getHuntingAreaType());
            assertEquals(Required.NO, formFields.getHuntingAreaSize());
            assertEquals(Required.NO, formFields.getHuntingParty());
            assertEquals(Required.NO, formFields.getReportedWithPhoneCall());
        }
    }

    // BEAR

    @Test
    public void testBear_Season_SpecimenFields() {
        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFields.getSpecimenFields(
                HUNTING_YEAR, GameSpecies.OFFICIAL_CODE_BEAR, null, HarvestReportingType.SEASON);

        assertEquals(Required.YES, specimenFields.getAge());
        assertEquals(Required.YES, specimenFields.getGender());
        assertEquals(Required.YES, specimenFields.getWeight());

        assertNoMooseFields(specimenFields);
    }

    @Test
    public void testBear_Season_FormFields() {
        final RequiredHarvestFields.Report formFields = RequiredHarvestFields.getFormFields(
                HUNTING_YEAR, GameSpecies.OFFICIAL_CODE_BEAR, HarvestReportingType.SEASON);

        assertEquals(Required.NO, formFields.getFeedingPlace());
        assertEquals(Required.YES, formFields.getHarvestArea());
        assertEquals(Required.NO, formFields.getHuntingMethod());
        assertEquals(Required.NO, formFields.getLukeStatus());
        assertEquals(Required.NO, formFields.getHuntingAreaType());
        assertEquals(Required.NO, formFields.getHuntingAreaSize());
        assertEquals(Required.NO, formFields.getHuntingParty());
        assertEquals(Required.NO, formFields.getReportedWithPhoneCall());
    }

    @Test
    public void testBear_Season_FormFields_Old() {
        final RequiredHarvestFields.Report formFields = RequiredHarvestFields.getFormFields(
                2014, GameSpecies.OFFICIAL_CODE_BEAR, HarvestReportingType.SEASON);

        assertEquals(Required.NO, formFields.getFeedingPlace());
        assertEquals(Required.YES, formFields.getHarvestArea());
        assertEquals(Required.NO, formFields.getHuntingMethod());
        assertEquals(Required.NO, formFields.getLukeStatus());
        assertEquals(Required.NO, formFields.getHuntingAreaType());
        assertEquals(Required.NO, formFields.getHuntingAreaSize());
        assertEquals(Required.NO, formFields.getHuntingParty());
        assertEquals(Required.YES, formFields.getReportedWithPhoneCall());
    }

    // GREY SEAL

    @Test
    public void testGreySealSeason_Season_SpecimenFields() {
        for (final HuntingMethod huntingMethod : HuntingMethod.values()) {
            final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFields.getSpecimenFields(
                    HUNTING_YEAR, GameSpecies.OFFICIAL_CODE_GREY_SEAL, huntingMethod, HarvestReportingType.SEASON);

            assertEquals(Required.YES, specimenFields.getAge());
            assertEquals(Required.YES, specimenFields.getGender());

            if (huntingMethod == HuntingMethod.SHOT_BUT_LOST) {
                assertEquals(Required.NO, specimenFields.getWeight());
            } else {
                assertEquals(Required.YES, specimenFields.getWeight());
            }
        }
    }

    @Test
    public void testGreySealSeason_Season_FormFields() {
        final RequiredHarvestFields.Report formFields = RequiredHarvestFields.getFormFields(
                HUNTING_YEAR, GameSpecies.OFFICIAL_CODE_GREY_SEAL, HarvestReportingType.SEASON);

        assertEquals(Required.NO, formFields.getFeedingPlace());
        assertEquals(Required.YES, formFields.getHarvestArea());
        assertEquals(Required.YES, formFields.getHuntingMethod());
        assertEquals(Required.NO, formFields.getLukeStatus());
        assertEquals(Required.NO, formFields.getHuntingAreaType());
        assertEquals(Required.NO, formFields.getHuntingAreaSize());
        assertEquals(Required.NO, formFields.getHuntingParty());
        assertEquals(Required.NO, formFields.getReportedWithPhoneCall());
    }

    // ROE DEER

    @Test
    public void testRoeDeer_Season_SpecimenFields() {
        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFields.getSpecimenFields(
                HUNTING_YEAR, GameSpecies.OFFICIAL_CODE_ROE_DEER, null, HarvestReportingType.SEASON);

        assertEquals(Required.YES, specimenFields.getAge());
        assertEquals(Required.YES, specimenFields.getGender());
        assertEquals(Required.VOLUNTARY, specimenFields.getWeight());

        assertNoMooseFields(specimenFields);
    }

    @Test
    public void testRoeDeer_Season_FormFields() {
        final RequiredHarvestFields.Report formFields = RequiredHarvestFields.getFormFields(
                HUNTING_YEAR, GameSpecies.OFFICIAL_CODE_ROE_DEER, HarvestReportingType.SEASON);

        assertEquals(Required.NO, formFields.getFeedingPlace());
        assertEquals(Required.NO, formFields.getHarvestArea());
        assertEquals(Required.NO, formFields.getHuntingMethod());
        assertEquals(Required.NO, formFields.getLukeStatus());
        assertEquals(Required.VOLUNTARY, formFields.getHuntingAreaType());
        assertEquals(Required.NO, formFields.getHuntingAreaSize());
        assertEquals(Required.NO, formFields.getHuntingParty());
        assertEquals(Required.NO, formFields.getReportedWithPhoneCall());
    }

    @Test
    public void testRoeDeer_Season_FormFields_Old() {
        final RequiredHarvestFields.Report formFields = RequiredHarvestFields.getFormFields(
                2016, GameSpecies.OFFICIAL_CODE_ROE_DEER, HarvestReportingType.SEASON);

        assertEquals(Required.NO, formFields.getFeedingPlace());
        assertEquals(Required.NO, formFields.getHarvestArea());
        assertEquals(Required.NO, formFields.getHuntingMethod());
        assertEquals(Required.NO, formFields.getLukeStatus());
        assertEquals(Required.YES, formFields.getHuntingAreaType());
        assertEquals(Required.YES, formFields.getHuntingAreaSize());
        assertEquals(Required.VOLUNTARY, formFields.getHuntingParty());
        assertEquals(Required.NO, formFields.getReportedWithPhoneCall());
    }

    // WOLF

    @Test
    public void testWolf_Season_SpecimenFields() {
        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFields.getSpecimenFields(
                2016, GameSpecies.OFFICIAL_CODE_WOLF, null, HarvestReportingType.SEASON);

        assertEquals(Required.VOLUNTARY, specimenFields.getAge());
        assertEquals(Required.VOLUNTARY, specimenFields.getGender());
        assertEquals(Required.VOLUNTARY, specimenFields.getWeight());

        assertNoMooseFields(specimenFields);
    }

    @Test
    public void testWolf_Season_FormFields() {
        final RequiredHarvestFields.Report formFields = RequiredHarvestFields.getFormFields(
                2016, GameSpecies.OFFICIAL_CODE_WOLF, HarvestReportingType.SEASON);

        assertEquals(Required.NO, formFields.getFeedingPlace());
        assertEquals(Required.NO, formFields.getHarvestArea());
        assertEquals(Required.NO, formFields.getHuntingMethod());
        assertEquals(Required.VOLUNTARY, formFields.getLukeStatus());
        assertEquals(Required.NO, formFields.getHuntingAreaType());
        assertEquals(Required.NO, formFields.getHuntingAreaSize());
        assertEquals(Required.NO, formFields.getHuntingParty());
        assertEquals(Required.NO, formFields.getReportedWithPhoneCall());
    }

    // WILD BOAR

    @Test
    public void testWildBoar_Season_SpecimenFields() {
        final RequiredHarvestFields.Specimen specimenFields = RequiredHarvestFields.getSpecimenFields(
                HUNTING_YEAR, GameSpecies.OFFICIAL_CODE_WILD_BOAR, null, HarvestReportingType.SEASON);

        assertEquals(Required.YES, specimenFields.getAge());
        assertEquals(Required.YES, specimenFields.getGender());
        assertEquals(Required.VOLUNTARY, specimenFields.getWeight());

        assertNoMooseFields(specimenFields);
    }

    @Test
    public void testWildBoar_Season_FormFields() {
        final RequiredHarvestFields.Report formFields = RequiredHarvestFields.getFormFields(
                HUNTING_YEAR, GameSpecies.OFFICIAL_CODE_WILD_BOAR, HarvestReportingType.SEASON);

        assertEquals(Required.VOLUNTARY, formFields.getFeedingPlace());
        assertEquals(Required.NO, formFields.getHarvestArea());
        assertEquals(Required.NO, formFields.getHuntingMethod());
        assertEquals(Required.NO, formFields.getLukeStatus());
        assertEquals(Required.NO, formFields.getHuntingAreaType());
        assertEquals(Required.NO, formFields.getHuntingAreaSize());
        assertEquals(Required.NO, formFields.getHuntingParty());
        assertEquals(Required.NO, formFields.getReportedWithPhoneCall());
    }

}
