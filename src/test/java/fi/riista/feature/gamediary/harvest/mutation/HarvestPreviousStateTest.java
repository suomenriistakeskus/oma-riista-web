package fi.riista.feature.gamediary.harvest.mutation;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HarvestPreviousStateTest implements ValueGeneratorMixin {

    private Harvest createInitialHarvest() {
        final GameSpecies species = new GameSpecies();
        species.setId(nextLong());

        final Harvest harvest = new Harvest();
        harvest.setSpecies(species);

        return harvest;
    }

    private Harvest createInitialHarvest_WithPermit() {
        final GameSpecies species = new GameSpecies();
        species.setId(nextLong());

        final HarvestPermit permit = new HarvestPermit();
        permit.setId(nextLong());

        final Harvest harvest = new Harvest();
        harvest.setSpecies(species);
        harvest.setHarvestPermit(permit);

        return harvest;
    }

    private Harvest createInitialHarvest_WithSeason() {
        final GameSpecies species = new GameSpecies();
        species.setId(nextLong());

        final HarvestSeason season = new HarvestSeason();
        season.setId(nextLong());

        final Harvest harvest = new Harvest();
        harvest.setSpecies(species);
        harvest.setHarvestSeason(season);

        return harvest;
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    // SEASON

    @Test
    public void testSpecies_Changed() {
        final Harvest harvest = createInitialHarvest();
        final HarvestPreviousState previousState = new HarvestPreviousState(harvest);

        final GameSpecies newSpecies = new GameSpecies();
        newSpecies.setId(nextLong());
        harvest.setSpecies(newSpecies);

        assertTrue(previousState.hasSpeciesChanged(harvest));
        assertFalse(previousState.hasPermitChanged(harvest));
        assertFalse(previousState.hasSeasonChanged(harvest));
        assertFalse(previousState.hasReportingTypeChanged(harvest));
        assertFalse(previousState.hasHarvestReportStateChanged(harvest));
        assertTrue(previousState.shouldInitReportState(harvest));
    }

    @Test
    public void testSpecies_NotChanged() {
        final Harvest harvest = createInitialHarvest();
        final HarvestPreviousState previousState = new HarvestPreviousState(harvest);

        assertFalse(previousState.hasSpeciesChanged(harvest));
        assertFalse(previousState.hasPermitChanged(harvest));
        assertFalse(previousState.hasSeasonChanged(harvest));
        assertFalse(previousState.hasReportingTypeChanged(harvest));
        assertFalse(previousState.hasHarvestReportStateChanged(harvest));
        assertFalse(previousState.shouldInitReportState(harvest));
    }

    // PERMIT

    @Test
    public void testPermit_Added() {
        final Harvest harvest = createInitialHarvest();
        final HarvestPreviousState previousState = new HarvestPreviousState(harvest);

        final HarvestPermit newPermit = new HarvestPermit();
        newPermit.setId(nextLong());
        harvest.setHarvestPermit(newPermit);

        assertFalse(previousState.hasSpeciesChanged(harvest));
        assertTrue(previousState.hasPermitChanged(harvest));
        assertFalse(previousState.hasSeasonChanged(harvest));
        assertTrue(previousState.hasReportingTypeChanged(harvest));
        assertFalse(previousState.hasHarvestReportStateChanged(harvest));
        assertTrue(previousState.shouldInitReportState(harvest));
    }

    @Test
    public void testPermit_Changed() {
        final Harvest harvest = createInitialHarvest_WithPermit();
        final HarvestPreviousState previousState = new HarvestPreviousState(harvest);

        final HarvestPermit newPermit = new HarvestPermit();
        newPermit.setId(nextLong());
        harvest.setHarvestPermit(newPermit);

        assertFalse(previousState.hasSpeciesChanged(harvest));
        assertTrue(previousState.hasPermitChanged(harvest));
        assertFalse(previousState.hasSeasonChanged(harvest));
        assertFalse(previousState.hasReportingTypeChanged(harvest));
        assertFalse(previousState.hasHarvestReportStateChanged(harvest));
        assertTrue(previousState.shouldInitReportState(harvest));
    }

    @Test
    public void testPermit_NotChanged() {
        final Harvest harvest = createInitialHarvest_WithPermit();
        final HarvestPreviousState previousState = new HarvestPreviousState(harvest);

        assertFalse(previousState.hasSpeciesChanged(harvest));
        assertFalse(previousState.hasPermitChanged(harvest));
        assertFalse(previousState.hasSeasonChanged(harvest));
        assertFalse(previousState.hasReportingTypeChanged(harvest));
        assertFalse(previousState.hasHarvestReportStateChanged(harvest));
        assertFalse(previousState.shouldInitReportState(harvest));
    }

    @Test
    public void testPermit_Removed() {
        final Harvest harvest = createInitialHarvest_WithPermit();
        final HarvestPreviousState previousState = new HarvestPreviousState(harvest);

        harvest.setHarvestPermit(null);

        assertFalse(previousState.hasSpeciesChanged(harvest));
        assertTrue(previousState.hasPermitChanged(harvest));
        assertFalse(previousState.hasSeasonChanged(harvest));
        assertTrue(previousState.hasReportingTypeChanged(harvest));
        assertFalse(previousState.hasHarvestReportStateChanged(harvest));
        assertTrue(previousState.shouldInitReportState(harvest));
    }

    // SEASON

    @Test
    public void testSeason_Added() {
        final Harvest harvest = createInitialHarvest();
        final HarvestPreviousState previousState = new HarvestPreviousState(harvest);

        final HarvestSeason newSeason = new HarvestSeason();
        newSeason.setId(nextLong());
        harvest.setHarvestSeason(newSeason);

        assertFalse(previousState.hasSpeciesChanged(harvest));
        assertFalse(previousState.hasPermitChanged(harvest));
        assertTrue(previousState.hasSeasonChanged(harvest));
        assertTrue(previousState.hasReportingTypeChanged(harvest));
        assertFalse(previousState.hasHarvestReportStateChanged(harvest));
        assertTrue(previousState.shouldInitReportState(harvest));
    }

    @Test
    public void testSeason_Changed() {
        final Harvest harvest = createInitialHarvest_WithSeason();
        final HarvestPreviousState previousState = new HarvestPreviousState(harvest);

        final HarvestSeason newSeason = new HarvestSeason();
        newSeason.setId(nextLong());
        harvest.setHarvestSeason(newSeason);

        assertFalse(previousState.hasSpeciesChanged(harvest));
        assertFalse(previousState.hasPermitChanged(harvest));
        assertTrue(previousState.hasSeasonChanged(harvest));
        assertFalse(previousState.hasReportingTypeChanged(harvest));
        assertFalse(previousState.hasHarvestReportStateChanged(harvest));
        assertTrue(previousState.shouldInitReportState(harvest));
    }

    @Test
    public void testSeason_NotChanged() {
        final Harvest harvest = createInitialHarvest_WithSeason();
        final HarvestPreviousState previousState = new HarvestPreviousState(harvest);

        assertFalse(previousState.hasSpeciesChanged(harvest));
        assertFalse(previousState.hasPermitChanged(harvest));
        assertFalse(previousState.hasSeasonChanged(harvest));
        assertFalse(previousState.hasReportingTypeChanged(harvest));
        assertFalse(previousState.hasHarvestReportStateChanged(harvest));
        assertFalse(previousState.shouldInitReportState(harvest));
    }

    @Test
    public void testSeason_Removed() {
        final Harvest harvest = createInitialHarvest_WithSeason();
        final HarvestPreviousState previousState = new HarvestPreviousState(harvest);

        harvest.setHarvestSeason(null);

        assertFalse(previousState.hasSpeciesChanged(harvest));
        assertFalse(previousState.hasPermitChanged(harvest));
        assertTrue(previousState.hasSeasonChanged(harvest));
        assertTrue(previousState.hasReportingTypeChanged(harvest));
        assertFalse(previousState.hasHarvestReportStateChanged(harvest));
        assertTrue(previousState.shouldInitReportState(harvest));
    }
}
