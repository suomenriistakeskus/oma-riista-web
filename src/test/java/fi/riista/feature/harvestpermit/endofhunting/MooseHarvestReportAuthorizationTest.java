package fi.riista.feature.harvestpermit.endofhunting;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.security.EntityPermission;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;
import java.util.stream.Stream;

public class MooseHarvestReportAuthorizationTest extends EmbeddedDatabaseTest {

    private HarvestPermit permit;
    private MooseHarvestReport mooseHarvestReport;

    private HuntingClub permitHolder;
    private HuntingClubGroup permitHolderGroup;

    private HuntingClub permitPartner;
    private HuntingClubGroup permitPartnerGroup;

    private Person person;

    @Before
    public void setUp() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        person = model().newPerson();
        permit = model().newHarvestPermit(rhy);

        final GameSpecies species = model().newGameSpecies();
        final HarvestPermitSpeciesAmount hpsa = model().newHarvestPermitSpeciesAmount(permit, species);

        mooseHarvestReport = model().newMooseHarvestReport(hpsa);

        permitHolder = model().newHuntingClub(rhy);
        permit.setPermitHolder(permitHolder);

        permitPartner = model().newHuntingClub(rhy);
        Stream.of(permitHolder, permitPartner).forEach(permit.getPermitPartners()::add);

        permitHolderGroup = model().newHuntingClubGroup(permitHolder, hpsa);
        permitPartnerGroup = model().newHuntingClubGroup(permitPartner, hpsa);
    }

    // permit contact person

    @Test
    public void testContactPerson() {
        permit.setOriginalContactPerson(person);
        doTest(createUser(person), EntityPermission.crud());
    }

    // moderator and admin

    @Test
    public void testModerator() {
        doTest(createNewModerator(), EntityPermission.crud());
    }

    @Test
    public void testAdmin() {
        doTest(createNewAdmin(), EntityPermission.crud());
    }

    // permit holder

    @Test
    public void testPermitHolderMember() {
        newOccupation(OccupationType.SEURAN_JASEN, permitHolder);
        doTest(createUser(person), EntityPermission.none());
    }

    @Test
    public void testPermitHolderContactPerson() {
        newOccupation(OccupationType.SEURAN_YHDYSHENKILO, permitHolder);
        doTest(createUser(person), EntityPermission.crud());
    }

    @Test
    public void testPermitHolderGroupMember() {
        newOccupation(OccupationType.RYHMAN_JASEN, permitHolderGroup);
        doTest(createUser(person), EntityPermission.none());
    }

    @Test
    public void testPermitHolderGroupHuntingLeader() {
        newOccupation(OccupationType.RYHMAN_METSASTYKSENJOHTAJA, permitHolderGroup);
        doTest(createUser(person), EntityPermission.crud());
    }

    // permit partner

    @Test
    public void testPermitPartnerMember() {
        newOccupation(OccupationType.SEURAN_JASEN, permitPartner);
        doTest(createUser(person), EntityPermission.none());
    }

    @Test
    public void testPermitPartnerContactPerson() {
        newOccupation(OccupationType.SEURAN_YHDYSHENKILO, permitPartner);
        doTest(createUser(person), EntityPermission.none());
    }

    @Test
    public void testPermitPartnerGroupMember() {
        newOccupation(OccupationType.RYHMAN_JASEN, permitPartnerGroup);
        doTest(createUser(person), EntityPermission.none());
    }

    @Test
    public void testPermitPartnerGroupHuntingLeader() {
        newOccupation(OccupationType.RYHMAN_METSASTYKSENJOHTAJA, permitPartnerGroup);
        doTest(createUser(person), EntityPermission.none());
    }

    private void doTest(final SystemUser user, final EnumSet<EntityPermission> granted) {
        onSavedAndAuthenticated(user, () -> {
            assertHasPermissions(mooseHarvestReport, granted);
            assertNoPermissions(mooseHarvestReport, EnumSet.complementOf(granted));
        });
    }

    private Occupation newOccupation(final OccupationType type, final Organisation org) {
        return model().newOccupation(org, person, type);
    }
}
