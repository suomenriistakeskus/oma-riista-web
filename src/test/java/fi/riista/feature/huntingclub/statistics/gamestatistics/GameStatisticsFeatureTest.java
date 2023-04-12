package fi.riista.feature.huntingclub.statistics.gamestatistics;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.deercensus.DeerCensus;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAndRemainingPopulation;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.BeaverAppearance;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.SpeciesEstimatedAppearance;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.SpeciesEstimatedAppearanceWithPiglets;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.TrendOfPopulationGrowth;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

public class GameStatisticsFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private GameStatisticsFeature gameStatisticsFeature;

    @Test
    public void testDeerCensusStatistics() {

        DeerCensus deerCensus = model().newDeerCensus();
        deerCensus.setFallowDeers(123);
        deerCensus.setRoeDeers(456);
        deerCensus.setWhiteTailDeers(789);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            Long huntingClubId = deerCensus.getHuntingClub().getId();

            GameStatisticsDTO gsDTO = gameStatisticsFeature.getDeerCensusStatisticsForHuntingClub(huntingClubId);
            assertEquals(gsDTO.getTimestamps().size(), 1);
            assertEquals(gsDTO.getTimestamps().get(0).getYear(), deerCensus.getObservationDate().getYear());
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.FALLOW_DEERS).size(), 1);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.FALLOW_DEERS).get(0).intValue(), 123);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.ROE_DEERS).get(0).intValue(), 456);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.WHITE_TAIL_DEERS).get(0).intValue(), 789);
        });
    }

    @Test
    public void testDeerStatistics() {

        final GameSpecies species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);
        final HarvestPermit permit = model().newHarvestPermit();
        final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);
        final HuntingClub club = model().newHuntingClub();
        BasicClubHuntingSummary basicClubHuntingSummary = model().newBasicHuntingSummary(speciesAmount, club, true);
        basicClubHuntingSummary.setHuntingEndDate(new LocalDate(2023, 10, 10));
        basicClubHuntingSummary.setAreaSizeAndPopulation(
                new AreaSizeAndRemainingPopulation()
                        .withRemainingPopulationInEffectiveArea(123)
                        .withRemainingPopulationInTotalArea(456));

        onSavedAndAuthenticated(createNewModerator(), () -> {

            GameStatisticsDTO gsDTO = gameStatisticsFeature.getDeerStatisticsForHuntingClub(club.getId());
            assertEquals(gsDTO.getTimestamps().size(), 1);
            assertEquals(gsDTO.getTimestamps().get(0).getYear(), basicClubHuntingSummary.getHuntingEndDate().getYear());
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.WHITE_TAIL_DEER_REMAINING_POPULATION_IN_EFFECTIVE_AREA).size(), 1);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.WHITE_TAIL_DEER_REMAINING_POPULATION_IN_EFFECTIVE_AREA).get(0).intValue(), 123);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.WHITE_TAIL_DEER_REMAINING_POPULATION_IN_TOTAL_AREA).get(0).intValue(), 456);
        });
    }

    @Test
    public void testMooseStatistics() {

        final GameSpecies species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_MOOSE);
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HuntingClub club = model().newHuntingClub(rhy);
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        final HarvestPermit permit = model().newMooselikePermit(rhy);
        permit.setHuntingClub(club);
        permit.setPermitHolder(PermitHolder.createHolderForClub(club));
        permit.getPermitPartners().add(club);
        group.updateHarvestPermit(permit);
        final GISHirvitalousalue hta = model().newGISHirvitalousalue();

        // Intermediary flush needed before persisting MooseHuntingSummary in order to have
        // harvest_permit_partners table populated required for foreign key constraint.
        persistInNewTransaction();

        MooseHuntingSummary mooseHuntingSummary = model().newMooseHuntingSummary(permit, club, true);
        mooseHuntingSummary.setHuntingEndDate(new LocalDate(2023, 10, 10));
        mooseHuntingSummary.setAreaSizeAndPopulation(
                new AreaSizeAndRemainingPopulation()
                        .withRemainingPopulationInEffectiveArea(123)
                        .withRemainingPopulationInTotalArea(456));

        mooseHuntingSummary.setNumberOfDrownedMooses(11);
        mooseHuntingSummary.setNumberOfMoosesKilledByBear(12);
        mooseHuntingSummary.setNumberOfMoosesKilledByWolf(13);
        mooseHuntingSummary.setNumberOfMoosesKilledInTrafficAccident(14);
        mooseHuntingSummary.setNumberOfMoosesKilledByPoaching(15);
        mooseHuntingSummary.setNumberOfMoosesKilledInRutFight(16);
        mooseHuntingSummary.setNumberOfStarvedMooses(17);
        mooseHuntingSummary.setNumberOfMoosesDeceasedByOtherReason(18);

        mooseHuntingSummary.setWhiteTailedDeerAppearance(
                new SpeciesEstimatedAppearance(true, TrendOfPopulationGrowth.UNCHANGED, 21));
        mooseHuntingSummary.setRoeDeerAppearance(
                new SpeciesEstimatedAppearance(true, TrendOfPopulationGrowth.UNCHANGED, 22));
        mooseHuntingSummary.setWildForestReindeerAppearance(
                new SpeciesEstimatedAppearance(true, TrendOfPopulationGrowth.UNCHANGED, 23));
        mooseHuntingSummary.setFallowDeerAppearance(
                new SpeciesEstimatedAppearance(true, TrendOfPopulationGrowth.UNCHANGED, 24));

        BeaverAppearance beaverAppearance = new BeaverAppearance();
        beaverAppearance.setAppeared(true);
        beaverAppearance.setHarvestAmount(31);
        beaverAppearance.setAmountOfInhabitedWinterNests(32);
        beaverAppearance.setAreaOfDamage(33);
        beaverAppearance.setAreaOccupiedByWater(34);
        mooseHuntingSummary.setBeaverAppearance(beaverAppearance);

        SpeciesEstimatedAppearanceWithPiglets wildBoarAppearance = new SpeciesEstimatedAppearanceWithPiglets();
        wildBoarAppearance.setEstimatedAmountOfSpecimens(41);
        wildBoarAppearance.setEstimatedAmountOfSowWithPiglets(42);
        mooseHuntingSummary.setWildBoarAppearance(wildBoarAppearance);

        onSavedAndAuthenticated(createNewModerator(), () -> {

            GameStatisticsDTO gsDTO = gameStatisticsFeature.getMooseStatisticsForHuntingClub(club.getId());
            assertEquals(gsDTO.getTimestamps().size(), 1);
            assertEquals(gsDTO.getTimestamps().get(0).getYear(), mooseHuntingSummary.getHuntingEndDate().getYear());
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.REMAINING_MOOSES_IN_EFFECTIVE_AREA).size(), 1);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.REMAINING_MOOSES_IN_EFFECTIVE_AREA).get(0).intValue(), 123);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.REMAINING_MOOSES_IN_TOTAL_AREA).get(0).intValue(), 456);

            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.DROWNED_MOOSES).get(0).intValue(), 11);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.MOOSES_KILLED_BY_BEAR).get(0).intValue(), 12);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.MOOSES_KILLED_BY_WOLF).get(0).intValue(), 13);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.MOOSES_KILLED_IN_TRAFFIC_ACCIDENT).get(0).intValue(), 14);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.MOOSES_KILLED_BY_POACHING).get(0).intValue(), 15);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.MOOSES_KILLED_IN_RUT_FIGHT).get(0).intValue(), 16);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.STARVED_MOOSES).get(0).intValue(), 17);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.MOOSES_DECEASED_BY_OTHER_REASON).get(0).intValue(), 18);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.TOTAL_DEAD_MOOSES).get(0).intValue(), 116);

            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.WHITE_TAIL_DEERS).get(0).intValue(), 21);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.ROE_DEERS).get(0).intValue(), 22);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.WILD_FOREST_REINDEERS).get(0).intValue(), 23);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.FALLOW_DEERS).get(0).intValue(), 24);

            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.BEAVERS_HARVEST_AMOUNT).get(0).intValue(), 31);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.BEAVERS_AMOUNT_OF_INHABITED_WINTER_NESTS).get(0).intValue(), 32);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.BEAVERS_AREA_OF_DAMAGE).get(0).intValue(), 33);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.BEAVERS_AREA_OCCUPIED_BY_WATER).get(0).intValue(), 34);

            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.WILD_BOARS_ESTIMATED_AMOUNT_OF_SPECIMENS).get(0).intValue(), 41);
            assertEquals(gsDTO.getDatasets().get(GameStatisticsDTO.WILD_BOARS_ESTIMATED_AMOUNT_OF_SOW_WITH_PIGLETS).get(0).intValue(), 42);
        });
    }
}
