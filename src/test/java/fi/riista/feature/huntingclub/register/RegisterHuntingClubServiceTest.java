package fi.riista.feature.huntingclub.register;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.lupahallinta.LHOrganisation;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RegisterHuntingClubServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private RegisterHuntingClubService service;

    @Test
    public void testNotExistsReturnsNull() {
        onSavedAndAuthenticated(createNewUser(), tx(() -> assertNull(service.findExistingOrCreate("1234567"))));
    }

    @Test
    public void testLhOrgExists() {
        final LHOrganisation lhOrg = model().newLHOrganisation();

        onSavedAndAuthenticated(createNewUser(),
                tx(() -> RegisterHuntingClubFeatureTest.assertClubEqualsLhOrg(lhOrg, service.findExistingOrCreate(lhOrg.getOfficialCode()))));
    }

    @Test
    public void testLhOrgAndClubExists() {
        withRhy(rhy -> {
            final LHOrganisation lhOrg = model().newLHOrganisation(rhy);
            final String officialCode = lhOrg.getOfficialCode();

            final HuntingClub club = model().newHuntingClub(rhy);
            club.setOfficialCode(officialCode);

            onSavedAndAuthenticated(
                    createNewUser(), tx(() -> RegisterHuntingClubFeatureTest.assertClubsEquals(club, service.findExistingOrCreate(officialCode))));
        });
    }

    @Test
    public void testLhOrgNotExistsButClubExists() {
        final HuntingClub club = model().newHuntingClub();

        onSavedAndAuthenticated(createNewUser(), tx(() -> {
            HuntingClub c = service.findExistingOrCreate(club.getOfficialCode());
            assertEquals(club.getId(), c.getId());
        }));
    }
}
