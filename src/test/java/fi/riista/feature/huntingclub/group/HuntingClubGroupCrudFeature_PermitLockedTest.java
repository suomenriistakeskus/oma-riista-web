package fi.riista.feature.huntingclub.group;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.junit.Test;

import javax.annotation.Resource;

public class HuntingClubGroupCrudFeature_PermitLockedTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubGroupCrudFeature huntingClubGroupCrudFeature;

    @Test
    public void testUpdateForContactPerson() {
        doUpdate(DateUtil.huntingYear() + 1, createUserWithPerson());
    }

    @Test(expected = HuntingGroupWithPermitLockedException.class)
    public void testUpdateForContactPerson2() {
        doUpdate(DateUtil.huntingYear() - 1, createUserWithPerson());
    }

    @Test
    public void testUpdateForModerator() {
        doUpdate(DateUtil.huntingYear() + 1, createNewModerator());
    }

    @Test
    public void testUpdateForModerator2() {
        doUpdate(DateUtil.huntingYear() - 1, createNewModerator());
    }

    private void doUpdate(final int huntingYear, final SystemUser user) {
        final HuntingClubGroup group = createGroup(huntingYear, user);
        onSavedAndAuthenticated(user, tx(() ->
                huntingClubGroupCrudFeature.update(huntingClubGroupCrudFeature.read(group.getId()))));
    }

    @Test
    public void testDeleteForContactPerson() {
        doDelete(DateUtil.huntingYear() + 1, createUserWithPerson());
    }

    @Test(expected = HuntingGroupWithPermitLockedException.class)
    public void testDeleteForContactPerson2() {
        doDelete(DateUtil.huntingYear() - 1, createUserWithPerson());
    }

    @Test
    public void testDeleteForModerator() {
        doDelete(DateUtil.huntingYear() + 1, createNewModerator());
    }

    @Test
    public void testDeleteForModerator2() {
        doDelete(DateUtil.huntingYear() - 1, createNewModerator());
    }

    private void doDelete(final int huntingYear, final SystemUser user) {
        final HuntingClubGroup group = createGroup(huntingYear, user);
        onSavedAndAuthenticated(user, tx(() -> huntingClubGroupCrudFeature.delete(group.getId())));
    }

    private HuntingClubGroup createGroup(final int huntingYear, final SystemUser user) {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermit harvestPermit = model().newMooselikePermit(rhy);
        final GameSpecies gameSpecies = model().newGameSpecies();
        final HarvestPermitSpeciesAmount spa = model().newHarvestPermitSpeciesAmount(harvestPermit, gameSpecies, huntingYear);
        final HuntingClub club = model().newHuntingClub();
        if (user.getPerson() != null) {
            model().newOccupation(club, user.getPerson(), OccupationType.SEURAN_YHDYSHENKILO);
        }
        return model().newHuntingClubGroup(club, spa);
    }
}
