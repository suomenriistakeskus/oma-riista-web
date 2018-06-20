package fi.riista.feature.huntingclub.area;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Resource;

public class HuntingClubAreaCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubAreaCrudFeature crudFeature;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testUpdateDetachedArea_CanUpdateAllFields() {
        HuntingClub club = model().newHuntingClub();
        Person contactPerson = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO).getPerson();
        SystemUser user = createUser(contactPerson);
        HuntingClubArea area = model().newHuntingClubArea(club, "FI", "SV", 2015);

        onSavedAndAuthenticated(user, () -> crudFeature.update(HuntingClubAreaDTO.create(area, club, null)));
    }

    @Test
    public void testUpdateAttachedArea_CanNotUpdateHuntingYear() {
        HuntingClub club = model().newHuntingClub();
        Person contactPerson = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO).getPerson();
        SystemUser user = createUser(contactPerson);
        HuntingClubArea area = model().newHuntingClubArea(club, "FI", "SV", 2015);
        HuntingClubGroup group = model().newHuntingClubGroupWithArea(area);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("huntingYear cannot be changed");

        onSavedAndAuthenticated(user, () -> {
            final HuntingClubAreaDTO dto = HuntingClubAreaDTO.create(area, club, null);
            dto.setHuntingYear(area.getHuntingYear() + 1);
            crudFeature.update(dto);
        });
    }

    @Test
    public void testUpdateAttachedArea_CanNotUpdateActiveStatus() {
        HuntingClub club = model().newHuntingClub();
        Person contactPerson = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO).getPerson();
        SystemUser user = createUser(contactPerson);
        HuntingClubArea area = model().newHuntingClubArea(club, "FI", "SV", 2015);
        HuntingClubGroup group = model().newHuntingClubGroupWithArea(area);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("area cannot be deactivated");

        onSavedAndAuthenticated(user, () -> {
            crudFeature.setActiveStatus(area.getId(), false);
        });
    }

    @Test
    public void testUpdateAttachedArea_CanUpdateName() {
        HuntingClub club = model().newHuntingClub();
        Person contactPerson = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO).getPerson();
        SystemUser user = createUser(contactPerson);
        HuntingClubArea area = model().newHuntingClubArea(club, "FI", "SV", 2015);
        HuntingClubGroup group = model().newHuntingClubGroupWithArea(area);

        onSavedAndAuthenticated(user, () -> {
            final HuntingClubAreaDTO dto = HuntingClubAreaDTO.create(area, club, null);
            dto.setNameFI("FI2");
            dto.setNameSV("SV2");
            crudFeature.update(dto);
        });
    }
}
