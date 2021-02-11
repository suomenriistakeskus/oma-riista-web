package fi.riista.feature.permit.decision;

import fi.riista.feature.common.decision.GrantStatus;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class PermitDecisionGrantStatusUpdaterTest {

    @Test
    public void testSmoke_Unchanged() {
        final List<HarvestPermitApplicationSpeciesAmount> a = singletonList(createApplicationAmount(1, 0.5f));
        final List<PermitDecisionSpeciesAmount> b = singletonList(createDecisionAmount(1, 0.5f));

        assertEquals(GrantStatus.UNCHANGED, calculate(a, b));
    }

    @Test
    public void testSmoke_Rejected() {
        final List<HarvestPermitApplicationSpeciesAmount> a = singletonList(createApplicationAmount(1, 0.5f));
        final List<PermitDecisionSpeciesAmount> b = singletonList(createDecisionAmount(1, 0f));

        assertEquals(GrantStatus.REJECTED, calculate(a, b));
    }

    @Test
    public void testSmoke_Restricted() {
        final List<HarvestPermitApplicationSpeciesAmount> a = singletonList(createApplicationAmount(1, 1.0f));
        final List<PermitDecisionSpeciesAmount> b = singletonList(createDecisionAmount(1, 0.5f));

        assertEquals(GrantStatus.RESTRICTED, calculate(a, b));
    }

    // Multiple species

    @Test
    public void testMultipleSpeciesOneYear_Unchanged() {
        final List<HarvestPermitApplicationSpeciesAmount> a = asList(
                createApplicationAmount(1, 0.5f),
                createApplicationAmount(2, 1.5f));

        final List<PermitDecisionSpeciesAmount> b = asList(
                createDecisionAmount(1, 0.5f),
                createDecisionAmount(2, 1.5f));

        assertEquals(GrantStatus.UNCHANGED, calculate(a, b));
    }

    @Test
    public void testMultipleSpeciesOneYear_Rejected() {
        final List<HarvestPermitApplicationSpeciesAmount> a = asList(
                createApplicationAmount(1, 0.5f),
                createApplicationAmount(2, 1.5f));

        final List<PermitDecisionSpeciesAmount> b = asList(
                createDecisionAmount(1, 0),
                createDecisionAmount(2, 0));

        assertEquals(GrantStatus.REJECTED, calculate(a, b));
    }

    @Test
    public void testMultipleSpeciesOneYear_Restricted() {
        final List<HarvestPermitApplicationSpeciesAmount> a = asList(
                createApplicationAmount(1, 0.5f),
                createApplicationAmount(2, 1.5f));

        final List<PermitDecisionSpeciesAmount> b = asList(
                createDecisionAmount(1, 0.5f),
                createDecisionAmount(2, 1.0f));

        assertEquals(GrantStatus.RESTRICTED, calculate(a, b));
    }

    // Multiple years

    @Test
    public void testMultipleSpeciesMultipleYear_Unchanged() {
        final List<HarvestPermitApplicationSpeciesAmount> a = asList(
                createApplicationAmount(1, 1.0f),
                createApplicationAmount(1, 3.0f),
                createApplicationAmount(2, 2.0f),
                createApplicationAmount(2, 5.0f));

        final List<PermitDecisionSpeciesAmount> b = asList(
                createDecisionAmount(1, 1.0f),
                createDecisionAmount(1, 3.0f),
                createDecisionAmount(2, 2.0f),
                createDecisionAmount(2, 5.0f));

        assertEquals(GrantStatus.UNCHANGED, calculate(a, b));

    }

    @Test
    public void testMultipleSpeciesMultipleYear_Rejected() {
        final List<HarvestPermitApplicationSpeciesAmount> a = asList(
                createApplicationAmount(1, 1.0f),
                createApplicationAmount(1, 3.0f),
                createApplicationAmount(2, 2.0f),
                createApplicationAmount(2, 5.0f));

        final List<PermitDecisionSpeciesAmount> b = asList(
                createDecisionAmount(1, 0),
                createDecisionAmount(1, 0),
                createDecisionAmount(2, 0),
                createDecisionAmount(2, 0));

        assertEquals(GrantStatus.REJECTED, calculate(a, b));
    }

    @Test
    public void testMultipleSpeciesMultipleYear_Restricted_TotalIsSmaller() {
        final List<HarvestPermitApplicationSpeciesAmount> a = asList(
                createApplicationAmount(1, 1.0f),
                createApplicationAmount(1, 3.0f),
                createApplicationAmount(2, 2.0f),
                createApplicationAmount(2, 5.0f));

        final List<PermitDecisionSpeciesAmount> b = asList(
                createDecisionAmount(1, 0),
                createDecisionAmount(1, 3.0f),
                createDecisionAmount(2, 0),
                createDecisionAmount(2, 5.0f));

        assertEquals(GrantStatus.RESTRICTED, calculate(a, b));
    }

    @Test
    public void testMultipleSpeciesMultipleYear_Restricted_TotalMatches() {
        final List<HarvestPermitApplicationSpeciesAmount> a = asList(
                createApplicationAmount(1, 1.0f),
                createApplicationAmount(1, 3.0f),
                createApplicationAmount(2, 2.0f),
                createApplicationAmount(2, 5.0f));

        final List<PermitDecisionSpeciesAmount> b = asList(
                createDecisionAmount(1, 2.0f),
                createDecisionAmount(1, 2.0f),
                createDecisionAmount(2, 3.0f),
                createDecisionAmount(2, 4.0f));

        assertEquals(GrantStatus.RESTRICTED, calculate(a, b));
    }

    private static GrantStatus calculate(final List<HarvestPermitApplicationSpeciesAmount> a,
                                         final List<PermitDecisionSpeciesAmount> b) {
        return new PermitDecisionGrantStatusUpdater(a, b).calculate();
    }

    private static HarvestPermitApplicationSpeciesAmount createApplicationAmount(final int speciesCode, final float amount) {
        final HarvestPermitApplicationSpeciesAmount speciesAmount = new HarvestPermitApplicationSpeciesAmount();
        final GameSpecies species = new GameSpecies();
        species.setOfficialCode(speciesCode);
        speciesAmount.setGameSpecies(species);
        speciesAmount.setSpecimenAmount(amount);
        return speciesAmount;
    }

    private static PermitDecisionSpeciesAmount createDecisionAmount(final int speciesCode, final float amount) {
        final PermitDecisionSpeciesAmount speciesAmount = new PermitDecisionSpeciesAmount();
        final GameSpecies species = new GameSpecies();
        species.setOfficialCode(speciesCode);
        speciesAmount.setGameSpecies(species);
        speciesAmount.setSpecimenAmount(amount);
        return speciesAmount;
    }
}
