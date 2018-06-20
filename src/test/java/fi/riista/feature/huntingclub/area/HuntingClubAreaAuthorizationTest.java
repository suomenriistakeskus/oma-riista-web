package fi.riista.feature.huntingclub.area;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.security.EntityPermission;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.security.EntityPermission.READ;

public class HuntingClubAreaAuthorizationTest extends EmbeddedDatabaseTest {

    @Test
    public void testMember() {
        withPerson(person -> {

            final HuntingClub club = model().newHuntingClub();
            final HuntingClubArea area = model().newHuntingClubArea(club);

            model().newOccupation(club, person, SEURAN_JASEN);

            onSavedAndAuthenticated(createUser(person), () -> assertHasPermission(area, READ));
        });
    }

    @Test
    public void testClubContact() {
        withPerson(person -> {

            final HuntingClub club = model().newHuntingClub();
            final HuntingClubArea area = model().newHuntingClubArea(club);

            model().newOccupation(club, person, SEURAN_YHDYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> assertHasPermissions(area, EntityPermission.crud()));
        });
    }
}
