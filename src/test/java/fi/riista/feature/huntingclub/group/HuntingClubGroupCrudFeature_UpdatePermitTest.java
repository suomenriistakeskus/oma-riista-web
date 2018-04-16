package fi.riista.feature.huntingclub.group;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class HuntingClubGroupCrudFeature_UpdatePermitTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubGroupCrudFeature huntingClubGroupCrudFeature;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Test
    public void testPermitModifiedTimestamp_existingChanged() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies();

        final HarvestPermit permit1 = model().newHarvestPermit(rhy);
        final HarvestPermit permit2 = model().newHarvestPermit(rhy);

        doTestPermitModifiedTimestamp(true,
                model().newHarvestPermitSpeciesAmount(permit1, species),
                model().newHarvestPermitSpeciesAmount(permit2, species));
    }

    @Test
    public void testPermitModifiedTimestamp_existingNulled() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermit permit = model().newHarvestPermit(rhy);
        final GameSpecies species = model().newGameSpecies();

        doTestPermitModifiedTimestamp(true,
                model().newHarvestPermitSpeciesAmount(permit, species),
                null);
    }

    @Test
    public void testPermitModifiedTimestamp_nullChanged() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies();
        final HarvestPermit permit = model().newHarvestPermit(rhy);

        doTestPermitModifiedTimestamp(true,
                null,
                model().newHarvestPermitSpeciesAmount(permit, species));
    }

    @Test
    public void testPermitModifiedTimestamp_nullKept() {
        doTestPermitModifiedTimestamp(false, null, null);
    }

    @Test
    public void testPermitModifiedTimestamp_existingKept() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermit permit = model().newHarvestPermit(rhy);
        final GameSpecies species = model().newGameSpecies();
        final HarvestPermitSpeciesAmount hpsa = model().newHarvestPermitSpeciesAmount(permit, species);

        doTestPermitModifiedTimestamp(false, hpsa, hpsa);
    }

    private void doTestPermitModifiedTimestamp(
            final boolean permitModificationTimeIsChanged,
            final HarvestPermitSpeciesAmount speciesAmount,
            final HarvestPermitSpeciesAmount speciesAmount2) {

        final HarvestPermit originalPermit = speciesAmount != null ? speciesAmount.getHarvestPermit() : null;
        final HarvestPermit newPermit = speciesAmount2 != null ? speciesAmount2.getHarvestPermit() : null;

        final GameSpecies species = speciesAmount != null
                ? speciesAmount.getGameSpecies()
                : speciesAmount2 != null ? speciesAmount2.getGameSpecies() : model().newGameSpecies();

        withRhy(rhy -> withPerson(person -> {
            final HuntingClub club = model().newHuntingClub(rhy);
            final HuntingClubArea area = model().newHuntingClubArea(club);
            final HuntingClubGroup group = model().newHuntingClubGroup(club, species);
            group.setHuntingYear(area.getHuntingYear());
            group.setHuntingArea(area);
            model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);

            group.updateHarvestPermit(originalPermit);

            onSavedAndAuthenticated(createUser(person), () -> {
                final Date originalPermitUpdateTime = group.getHarvestPermitModificationTime();
                huntingClubGroupCrudFeature.update(HuntingClubGroupDTO.create(group, group.getSpecies(), newPermit));

                runInTransaction(() -> {
                    final HuntingClubGroup updatedGroup = huntingClubGroupRepository.getOne(group.getId());

                    if (permitModificationTimeIsChanged) {
                        assertNotEquals(originalPermitUpdateTime, updatedGroup.getHarvestPermitModificationTime());
                    } else {
                        assertEquals(originalPermitUpdateTime, updatedGroup.getHarvestPermitModificationTime());
                    }
                });
            });
        }));
    }
}
