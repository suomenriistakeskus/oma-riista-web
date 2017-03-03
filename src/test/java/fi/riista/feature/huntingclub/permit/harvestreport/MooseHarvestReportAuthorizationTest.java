package fi.riista.feature.huntingclub.permit.harvestreport;

import fi.riista.feature.EmbeddedDatabaseTest;
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
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;
import java.util.stream.Stream;

import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

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
        SystemUser user = createUser(person);
        doTest(user, EnumSet.of(CREATE, READ, UPDATE, DELETE));
    }

    // moderator and admin

    @Test
    public void testModerator() {
        doTest(createNewModerator(), EnumSet.of(CREATE, READ, UPDATE, DELETE));
    }

    @Test
    public void testAdmin() {
        doTest(createNewAdmin(), EnumSet.of(CREATE, READ, UPDATE, DELETE));
    }

    // permit holder

    @Test
    public void testPermitHolderMember() {
        newOccupation(OccupationType.SEURAN_JASEN, permitHolder);
        SystemUser user = createUser(person);
        doTest(user, EnumSet.noneOf(EntityPermission.class));
    }

    @Test
    public void testPermitHolderContactPerson() {
        newOccupation(OccupationType.SEURAN_YHDYSHENKILO, permitHolder);
        SystemUser user = createUser(person);
        doTest(user, EnumSet.of(CREATE, READ, UPDATE, DELETE));
    }

    @Test
    public void testPermitHolderGroupMember() {
        newOccupation(OccupationType.RYHMAN_JASEN, permitHolderGroup);
        SystemUser user = createUser(person);
        doTest(user, EnumSet.noneOf(EntityPermission.class));
    }

    @Test
    public void testPermitHolderGroupHuntingLeader() {
        newOccupation(OccupationType.RYHMAN_METSASTYKSENJOHTAJA, permitHolderGroup);
        SystemUser user = createUser(person);
        doTest(user, EnumSet.of(CREATE, READ, UPDATE, DELETE));
    }

    // permit partner

    @Test
    public void testPermitPartnerMember() {
        newOccupation(OccupationType.SEURAN_JASEN, permitPartner);
        SystemUser user = createUser(person);
        doTest(user, EnumSet.noneOf(EntityPermission.class));
    }

    @Test
    public void testPermitPartnerContactPerson() {
        newOccupation(OccupationType.SEURAN_YHDYSHENKILO, permitPartner);
        SystemUser user = createUser(person);
        doTest(user, EnumSet.noneOf(EntityPermission.class));
    }

    @Test
    public void testPermitPartnerGroupMember() {
        newOccupation(OccupationType.RYHMAN_JASEN, permitPartnerGroup);
        SystemUser user = createUser(person);
        doTest(user, EnumSet.noneOf(EntityPermission.class));
    }

    @Test
    public void testPermitPartnerGroupHuntingLeader() {
        newOccupation(OccupationType.RYHMAN_METSASTYKSENJOHTAJA, permitPartnerGroup);
        SystemUser user = createUser(person);
        doTest(user, EnumSet.noneOf(EntityPermission.class));
    }

    private void doTest(SystemUser user, EnumSet<EntityPermission> granted) {
        onSavedAndAuthenticated(user, tx(() -> {
            doAsserts(true, granted);
            doAsserts(false, EnumSet.complementOf(granted));
        }));
    }

    private void doAsserts(boolean expected, EnumSet<EntityPermission> permissions) {
        assertHasPermissions(expected, mooseHarvestReport, permissions);
    }

    private Occupation newOccupation(OccupationType type, Organisation org) {
        return model().newOccupation(org, person, type);
    }
}
