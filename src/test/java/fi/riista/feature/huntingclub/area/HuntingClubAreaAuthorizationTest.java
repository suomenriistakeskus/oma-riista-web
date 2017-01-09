package fi.riista.feature.huntingclub.area;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;

import org.junit.Test;

public class HuntingClubAreaAuthorizationTest extends EmbeddedDatabaseTest {

    @Test
    public void test() {
        final HuntingClub club = model().newHuntingClub();
        final Person member1 = model().newHuntingClubMember(club, OccupationType.SEURAN_JASEN).getPerson();
        final Person member2 = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO).getPerson();
        final HuntingClubArea area = model().newHuntingClubArea(club);

        final SystemUser jasen = createNewUser("jasen", member1);
        final SystemUser yhdyshenkilo = createNewUser("yhdyshenkilo", member2);

        persistInNewTransaction();

        runInTransaction(() -> {
            authenticate(jasen);
            assertHasPermission(area, EntityPermission.READ);

            authenticate(yhdyshenkilo);
            assertHasPermission(area, EntityPermission.CREATE);
            assertHasPermission(area, EntityPermission.READ);
            assertHasPermission(area, EntityPermission.UPDATE);
            assertHasPermission(area, EntityPermission.DELETE);
        });
    }
}
