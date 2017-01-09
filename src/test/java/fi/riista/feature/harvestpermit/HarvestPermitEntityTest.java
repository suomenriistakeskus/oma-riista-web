package fi.riista.feature.harvestpermit;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import javaslang.Tuple;
import javaslang.Tuple3;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HarvestPermitEntityTest extends EmbeddedDatabaseTest {

    @Test
    public void testEndOfHuntingReportRequired() {
        HarvestPermit permit = new HarvestPermit();
        GameSpecies species1 = species(1);
        permit.getSpeciesAmounts().add(speciesAmount(2, species1));
        GameSpecies species2 = species(2);
        permit.getSpeciesAmounts().add(speciesAmount(2, species2));

        permit.setHarvestReports(harvestReports(
                Tuple.of(species1, HarvestReport.State.APPROVED, 1),
                Tuple.of(species1, HarvestReport.State.REJECTED, 1),
                Tuple.of(species2, HarvestReport.State.APPROVED, 2)));

        assertTrue(permit.isEndOfHuntingReportRequired());
    }

    @Test
    public void testEndOfHuntingReportNotRequired() {
        HarvestPermit permit = new HarvestPermit();
        GameSpecies species1 = species(1);
        permit.getSpeciesAmounts().add(speciesAmount(2, species1));
        GameSpecies species2 = species(2);
        permit.getSpeciesAmounts().add(speciesAmount(2, species2));

        permit.setHarvestReports(harvestReports(
                Tuple.of(species1, HarvestReport.State.APPROVED, 1),
                Tuple.of(species1, HarvestReport.State.APPROVED, 1),
                Tuple.of(species1, HarvestReport.State.REJECTED, 1),
                Tuple.of(species2, HarvestReport.State.APPROVED, 2)));

        assertFalse(permit.isEndOfHuntingReportRequired());
    }

    @SafeVarargs
    private static Set<HarvestReport> harvestReports(Tuple3<GameSpecies, HarvestReport.State, Integer>... reportSpec) {
        Set<HarvestReport> s = new HashSet<>();
        for (Tuple3<GameSpecies, HarvestReport.State, Integer> p: reportSpec) {
            HarvestReport report = new HarvestReport();

            Harvest harvest = new Harvest();
            harvest.setSpecies(p._1());
            harvest.setAmount(p._3());
            report.addHarvest(harvest);

            report.setState(p._2());
            s.add(report);
        }
        return s;
    }

    private static GameSpecies species(long id) {
        GameSpecies g = new GameSpecies();
        g.setId(id);
        return g;
    }

    private static HarvestPermitSpeciesAmount speciesAmount(float amount, GameSpecies species) {
        HarvestPermitSpeciesAmount spa = new HarvestPermitSpeciesAmount();
        spa.setAmount(amount);
        spa.setGameSpecies(species);
        return spa;
    }
}
