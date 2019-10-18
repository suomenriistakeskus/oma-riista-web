package fi.riista.feature.permit.decision.publish;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import org.junit.Test;

import static fi.riista.feature.permit.decision.publish.HarvestPermitModificationRestriction.hasSingleAmountForHarvestSpecies;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HarvestPermitModificationRestrictionTest {

    @Test
    public void testSingleDecisionSpeciesAmount() {
        final GameSpecies g1 = new GameSpecies();
        g1.setId(1L);
        final GameSpecies g2 = new GameSpecies();
        g2.setId(2L);

        final HarvestPermitSpeciesAmount spa1 = new HarvestPermitSpeciesAmount();
        spa1.setGameSpecies(g1);
        final HarvestPermitSpeciesAmount spa2 = new HarvestPermitSpeciesAmount();
        spa2.setGameSpecies(g2);

        assertTrue(hasSingleAmountForHarvestSpecies(asList(spa1, spa2)));
    }

    @Test
    public void testSingleDecisionSpeciesAmount_Empty() {
        assertTrue(hasSingleAmountForHarvestSpecies(emptyList()));
    }

    @Test
    public void testSingleDecisionSpeciesAmount_Duplicate() {
        final GameSpecies g1 = new GameSpecies();
        g1.setId(1L);

        final HarvestPermitSpeciesAmount spa1 = new HarvestPermitSpeciesAmount();
        spa1.setGameSpecies(g1);
        final HarvestPermitSpeciesAmount spa2 = new HarvestPermitSpeciesAmount();
        spa2.setGameSpecies(g1);

        assertFalse(hasSingleAmountForHarvestSpecies(asList(spa1, spa2)));
    }

}
