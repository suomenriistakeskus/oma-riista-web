package fi.riista.feature.huntingclub.moosedatacard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.harvest.specimen.GameAntlersType;
import fi.riista.feature.gamediary.harvest.specimen.GameFitnessClass;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardGameSpeciesAppearance;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardHuntingDay;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardLargeCarnivoreObservation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseCalf;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseFemale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardObservation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage7;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage8;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_1;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_3;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_4;
import fi.riista.util.DateUtil;

import org.joda.time.LocalDate;
import org.junit.Test;

// A test class for test class
public class MooseDataCardObjectFactoryTest {

    @Test
    public void testNewHuntingDay() {
        final LocalDate date = DateUtil.today();
        final MooseDataCardHuntingDay huntingDay = MooseDataCardObjectFactory.newHuntingDay(date);
        assertNotNull(huntingDay);
        assertEquals(date, huntingDay.getStartDate());
        assertNotNull(huntingDay.getHuntingTime());
        assertNotNull(huntingDay.getHuntingMethod());
        assertNotNull(huntingDay.getNumberOfHunters());
        assertNotNull(huntingDay.getNumberOfHounds());
    }

    @Test
    public void testNewMooseObservation() {
        final MooseDataCardObservation observation = MooseDataCardObjectFactory.newMooseObservation();
        assertNotNull(observation);
        assertNotNull(observation.getDate());
        assertNotNull(observation.getGeoLocation());
        assertNotNull(observation.getAU());
        assertNotNull(observation.getN0());
        assertNotNull(observation.getN1());
        assertNotNull(observation.getN2());
        assertNotNull(observation.getN3());
        assertNotNull(observation.getT());
    }

    @Test
    public void testNewMooseMale() {
        final MooseDataCardMooseMale male = MooseDataCardObjectFactory.newMooseMale();
        assertNotNull(male);
        assertNotNull(male.getDate());
        assertNotNull(male.getGeoLocation());
        assertNotNull(male.getWeightMeasured());
        assertNotNull(male.getWeightEstimated());
        assertTrue(HasMooseDataCardEncoding.eitherInvalidOrValid(GameAntlersType.class, male.getAntlersType()).isRight());
        assertNotNull(male.getAntlersWidth());
        assertNotNull(male.getAntlerPointsLeft());
        assertNotNull(male.getAntlerPointsRight());
        assertFitnessClass(male);
        assertNotNull(male.getAdditionalInfo());
    }

    @Test
    public void testNewMooseFemale() {
        final MooseDataCardMooseFemale female = MooseDataCardObjectFactory.newMooseFemale();
        assertNotNull(female);
        assertNotNull(female.getDate());
        assertNotNull(female.getGeoLocation());
        assertNotNull(female.getWeightMeasured());
        assertNotNull(female.getWeightEstimated());
        assertFitnessClass(female);
        assertNotNull(female.getAdditionalInfo());
    }

    @Test
    public void testNewMooseCalf() {
        final MooseDataCardMooseCalf calf = MooseDataCardObjectFactory.newMooseCalf();
        assertNotNull(calf);
        assertNotNull(calf.getDate());
        assertNotNull(calf.getGeoLocation());
        assertTrue(HasMooseDataCardEncoding.eitherInvalidOrValid(GameGender.class, calf.getGender()).isRight());
        assertNotNull(calf.getWeightMeasured());
        assertNotNull(calf.getWeightEstimated());
        assertFitnessClass(calf);
        assertNotNull(calf.getAdditionalInfo());
    }

    @Test
    public void testNewLargeCarnivoreObservation() {
        final MooseDataCardLargeCarnivoreObservation observation =
                MooseDataCardObjectFactory.newLargeCarnivoreObservation();

        assertNotNull(observation);
        assertNotNull(observation.getDate());
        assertNotNull(observation.getGeoLocation());
        assertTrue(HasMooseDataCardEncoding.eitherInvalidOrValid(ObservationType.class, observation.getObservationType()).isRight());
        assertNotNull(observation.getNumberOfWolves());
        assertNotNull(observation.getNumberOfBears());
        assertNotNull(observation.getNumberOfLynxes());
        assertNotNull(observation.getNumberOfWolverines());
        assertNotNull(observation.getAdditionalInfo());
    }

    private static void assertFitnessClass(final MooseDataCardHarvest harvest) {
        assertTrue(HasMooseDataCardEncoding.eitherInvalidOrValid(GameFitnessClass.class, harvest.getFitnessClass()).isRight());
    }

    @Test
    public void testNewPage7() {
        final MooseDataCardPage7 page7 = MooseDataCardObjectFactory.newPage7();

        assertEquals(MooseDataCardGameSpeciesAppearance.YES, page7.getWhiteTailedDeerAppeared());
        assertNotNull(page7.getEstimatedSpecimenAmountOfWhiteTailedDeer());
        assertNotNull(page7.getTrendOfWhiteTailedDeerPopulationGrowth());

        assertEquals(MooseDataCardGameSpeciesAppearance.YES, page7.getRoeDeerAppeared());
        assertNotNull(page7.getEstimatedSpecimenAmountOfRoeDeer());
        assertNotNull(page7.getTrendOfRoeDeerPopulationGrowth());

        assertEquals(MooseDataCardGameSpeciesAppearance.YES, page7.getWildForestReindeerAppeared());
        assertNotNull(page7.getEstimatedSpecimenAmountOfWildForestReindeer());
        assertNotNull(page7.getTrendOfWildForestReindeerPopulationGrowth());

        assertEquals(MooseDataCardGameSpeciesAppearance.YES, page7.getFallowDeerAppeared());
        assertNotNull(page7.getEstimatedSpecimenAmountOfFallowDeer());
        assertNotNull(page7.getTrendOfFallowDeerPopulationGrowth());
    }

    @Test
    public void testNewPage8() {
        final MooseDataCardPage8 page8 = MooseDataCardObjectFactory.newPage8();

        assertNotNull(page8.getHuntingEndDate());

        final MooseDataCardSection_8_1 section81 = page8.getSection_8_1();
        assertNotNull(section81);
        assertNotNull(section81.getTotalHuntingArea());
        assertNotNull(section81.getEffectiveHuntingArea());
        assertNull(section81.getEffectiveHuntingAreaPercentage());
        assertNotNull(section81.getMoosesRemainingInTotalHuntingArea());
        assertNotNull(section81.getMoosesRemainingInEffectiveHuntingArea());
        assertNotNull(section81.getHuntingAreaType());

        final MooseDataCardSection_8_3 section83 = page8.getSection_8_3();
        assertNotNull(section83);
        assertNotNull(section83.getNumberOfDrownedMooses());
        assertNotNull(section83.getNumberOfMoosesKilledByBear());
        assertNotNull(section83.getNumberOfMoosesKilledByWolf());
        assertNotNull(section83.getNumberOfMoosesKilledInTrafficAccident());
        assertNotNull(section83.getNumberOfMoosesKilledInPoaching());
        assertNotNull(section83.getNumberOfMoosesKilledInRutFight());
        assertNotNull(section83.getNumberOfStarvedMooses());
        assertNotNull(section83.getNumberOfMoosesDeceasedByOtherReason());
        assertNotNull(section83.getExplanationForOtherReason());

        final MooseDataCardSection_8_4 section84 = page8.getSection_8_4();
        assertNotNull(section84);
        assertNotNull(section84.getMooseHeatBeginDate());
        assertNotNull(section84.getMooseHeatEndDate());
        assertNotNull(section84.getMooseFawnBeginDate());
        assertNotNull(section84.getMooseFawnEndDate());
        assertEquals(MooseDataCardGameSpeciesAppearance.YES, section84.getDeerFlyAppearead());
        assertNotNull(section84.getDateOfFirstDeerFlySeen());
        assertNotNull(section84.getDateOfLastDeerFlySeen());
        assertNotNull(section84.getNumberOfAdultMoosesHavingFlies());
        assertNotNull(section84.getNumberOfYoungMoosesHavingFlies());
        assertNotNull(section84.getTrendOfDeerFlyPopulationGrowth());
    }

}
