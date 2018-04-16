package fi.riista.feature.huntingclub.area;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HuntingClubAreaSizeServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubAreaSizeService huntingClubAreaSizeService;

    private void assertPermitAreaSize(final long permitId,
                                      final long clubId,
                                      final Double expectedAreaSize) {
        runInTransaction(() -> {
            final HarvestPermit permit = entityManager().find(HarvestPermit.class, permitId);
            final HuntingClub huntingClub = entityManager().find(HuntingClub.class, clubId);
            final Optional<Double> calculatedArea = huntingClubAreaSizeService.getHuntingPermitAreaSize(permit, huntingClub);

            if (expectedAreaSize != null) {
                assertTrue(calculatedArea.isPresent());
                assertEquals(expectedAreaSize, calculatedArea.get(), 0.001);
            } else {
                assertFalse(calculatedArea.isPresent());
            }
        });
    }

    private HuntingClubArea createArea(final HuntingClub huntingClub, final double computedAreaSize) {
        return model().newHuntingClubArea(huntingClub, model().newGISZone(computedAreaSize));
    }

    private HuntingClubGroup createGroup(final HarvestPermit harvestPermit,
                                         final HuntingClub huntingClub,
                                         final GameSpecies gameSpecies,
                                         final HuntingClubArea area) {
        final HuntingClubGroup huntingClubGroup = model().newHuntingClubGroup(huntingClub, gameSpecies);
        huntingClubGroup.updateHarvestPermit(harvestPermit);
        huntingClubGroup.setHuntingArea(area);
        return huntingClubGroup;
    }

    @Test
    public void testGetHuntingPermitAreaSize() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies();
        final HarvestPermit permit = model().newMooselikePermit(rhy);

        final HuntingClub club = model().newHuntingClub(rhy);
        permit.getPermitPartners().add(club);

        createGroup(permit, club, species, createArea(club, 12345.0));

        persistInNewTransaction();
        assertPermitAreaSize(permit.getId(), club.getId(), 12345.0);
    }

    @Test
    public void testGetHuntingPermitAreaSize_MultipleGroupsWithSameArea() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies();

        final HarvestPermit permit = model().newMooselikePermit(rhy);

        final HuntingClub club = model().newHuntingClub(rhy);
        permit.getPermitPartners().add(club);

        // Shares same area as previous group.
        final HuntingClubArea sharedArea = createArea(club, 12345.0);
        createGroup(permit, club, species, sharedArea);
        createGroup(permit, club, species, sharedArea);

        persistInNewTransaction();
        assertPermitAreaSize(permit.getId(), club.getId(), 12345.0);
    }

    @Test
    public void testGetHuntingPermitAreaSize_MultipleGroupsWithDifferentArea() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies();

        final HarvestPermit permit = model().newMooselikePermit(rhy);

        final HuntingClub club = model().newHuntingClub(rhy);
        permit.getPermitPartners().add(club);

        createGroup(permit, club, species, createArea(club, 12345.0));
        createGroup(permit, club, species, createArea(club, 23456.0));

        // No result, because cannot calculate size for potential overlapping areas
        persistInNewTransaction();
        assertPermitAreaSize(permit.getId(), club.getId(), null);
    }

    @Test
    public void testGetHuntingPermitAreaSize_MultipleClubs() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies();
        final HarvestPermit permit = model().newMooselikePermit(rhy);

        final HuntingClub club1 = model().newHuntingClub(rhy);
        final HuntingClub club2 = model().newHuntingClub(rhy);
        permit.getPermitPartners().addAll(asList(club1, club2));

        createGroup(permit, club1, species, createArea(club1, 12345.0));
        createGroup(permit, club2, species, createArea(club2, 23456.0));

        persistInNewTransaction();
        assertPermitAreaSize(permit.getId(), club1.getId(), 12345.0);
        assertPermitAreaSize(permit.getId(), club2.getId(), 23456.0);
    }

    @Test
    public void testGetHuntingPermitAreaSize_MultipleGroupsWithDifferentPermit() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies();
        final HarvestPermit permit1 = model().newMooselikePermit(rhy);
        final HarvestPermit permit2 = model().newMooselikePermit(rhy);

        final HuntingClub club = model().newHuntingClub(rhy);
        permit1.getPermitPartners().add(club);
        permit2.getPermitPartners().add(club);

        createGroup(permit1, club, species, createArea(club, 12345.0));
        createGroup(permit2, club, species, createArea(club, 23456));

        persistInNewTransaction();
        assertPermitAreaSize(permit1.getId(), club.getId(), 12345.0);
        assertPermitAreaSize(permit2.getId(), club.getId(), 23456.0);
    }
}
