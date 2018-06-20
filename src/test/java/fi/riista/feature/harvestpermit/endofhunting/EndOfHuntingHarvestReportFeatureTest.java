package fi.riista.feature.harvestpermit.endofhunting;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EndOfHuntingHarvestReportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private EndOfHuntingHarvestReportFeature endOfHuntingHarvestReportFeature;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Test
    public void testCreateForPermit() {
        withPerson(person -> {
            final GameSpecies species = model().newGameSpecies(true);
            final HarvestPermit permit = createPermit(person, species);

            final Harvest h1 = createHarvest(species, permit, Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
            createHarvest(species, permit, Harvest.StateAcceptedToHarvestPermit.REJECTED);

            onSavedAndAuthenticated(createUser(person), () -> {
                endOfHuntingHarvestReportFeature.createEndOfHuntingReport(permit.getId());

                runInTransaction(() -> {
                    HarvestPermit reloadedPermit = harvestPermitRepository.getOne(permit.getId());
                    assertEquals(person, reloadedPermit.getHarvestReportAuthor());

                    Assert.assertEquals(HarvestReportState.SENT_FOR_APPROVAL, reloadedPermit.getHarvestReportState());

                    final List<Harvest> acceptedHarvest = reloadedPermit.getAcceptedHarvestForEndOfHuntingReport();
                    assertEquals(1, acceptedHarvest.size());
                    assertTrue(acceptedHarvest.contains(h1));

                    final Harvest harvest = acceptedHarvest.iterator().next();
                    assertEquals(permit, harvest.getHarvestPermit());
                });
            });
        });
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateForPermit_failsIfProposed() {
        withPerson(person -> {
            final GameSpecies species = model().newGameSpecies(true);
            final HarvestPermit permit = createPermit(person, species);

            createHarvest(species, permit, Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
            createHarvest(species, permit, Harvest.StateAcceptedToHarvestPermit.PROPOSED);

            onSavedAndAuthenticated(createUser(person), () -> {
                endOfHuntingHarvestReportFeature.createEndOfHuntingReport(permit.getId());
            });
        });
    }

    private Harvest createHarvest(
            final GameSpecies species, final HarvestPermit permit, final Harvest.StateAcceptedToHarvestPermit state) {

        final Harvest harvest = model().newHarvest(species);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(state);
        harvest.setRhy(permit.getRhy());
        return harvest;
    }

    private HarvestPermit createPermit(final Person person, final GameSpecies species) {
        final HarvestPermit permit = model().newHarvestPermit(true);
        permit.setOriginalContactPerson(person);
        model().newHarvestPermitSpeciesAmount(permit, species);
        return permit;
    }
}
