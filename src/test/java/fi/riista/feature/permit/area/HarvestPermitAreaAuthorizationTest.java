package fi.riista.feature.permit.area;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.security.EntityPermission.READ;

public class HarvestPermitAreaAuthorizationTest extends EmbeddedDatabaseTest {

    @Test
    public void testClubMemberHasReadPermission() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation clubMember = model().newHuntingClubMember(club, SEURAN_JASEN);
        final HarvestPermitArea permitArea = model().newHarvestPermitArea(club);

        onSavedAndAuthenticated(createUser(clubMember.getPerson()), () -> assertHasPermission(permitArea, READ));
    }

    @Test
    public void testPartnerMemberHasReadPermission() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation clubMember = model().newHuntingClubMember(club, SEURAN_JASEN);
        final HarvestPermitArea permitArea = model().newHarvestPermitArea();
        final HuntingClubArea source = model().newHuntingClubArea(club, model().newGISZone(123000.0));
        model().newHarvestPermitAreaPartner(permitArea, source);

        onSavedAndAuthenticated(createUser(clubMember.getPerson()), () -> assertHasPermission(permitArea, READ));
    }

    @Test
    public void otherClubMemberHasNoReadPermission() {
        final HuntingClub otherClub = model().newHuntingClub();
        final Occupation otherClubMember = model().newHuntingClubMember(otherClub, SEURAN_JASEN);

        final HuntingClub club = model().newHuntingClub();
        model().newHuntingClubMember(club, SEURAN_JASEN);
        final HarvestPermitArea permitArea = model().newHarvestPermitArea(club);

        onSavedAndAuthenticated(createUser(otherClubMember.getPerson()), () -> assertNoPermission(permitArea, READ));
    }

    @Test
    public void testQueryCountOnManyPartners() {
        withRhy(rhy -> {
            final HuntingClub club = model().newHuntingClub(rhy);
            final Occupation clubMember = model().newHuntingClubMember(club, SEURAN_JASEN);
            final HarvestPermitArea permitArea = model().newHarvestPermitArea();
            final HuntingClubArea source = model().newHuntingClubArea(club, model().newGISZone(123000.0));
            model().newHarvestPermitAreaPartner(permitArea, source);
            for (int i = 0; i < 33; i++) {
                createPartner(permitArea, rhy);
            }

            onSavedAndAuthenticated(createUser(clubMember.getPerson()), () -> {
                onCheckingPermission(READ).expect(true).expectNumberOfQueriesAtMost(7).apply(permitArea);
            });
        });
    }

    private void createPartner(HarvestPermitArea permitArea, Riistanhoitoyhdistys rhy) {
        final HuntingClub club = model().newHuntingClub(rhy);
        model().newHuntingClubMember(club, SEURAN_JASEN);
        final HuntingClubArea source = model().newHuntingClubArea(club, model().newGISZone(123000.0));
        model().newHarvestPermitAreaPartner(permitArea, source);
    }
}
