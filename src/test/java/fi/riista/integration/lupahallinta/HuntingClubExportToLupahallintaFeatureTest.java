package fi.riista.integration.lupahallinta;

import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.integration.lupahallinta.club.LHHuntingClubCSVRow;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HuntingClubExportToLupahallintaFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubExportToLupahallintaFeature feature;

    private RiistakeskuksenAlue rka;

    @Before
    public void initRka() {
        this.rka = model().newRiistakeskuksenAlue();
    }

    @Test(expected = AccessDeniedException.class)
    public void testNoPrivilege() {
        onSavedAndAuthenticated(createNewApiUser(), feature::exportToCSCV);
    }

    @Test
    public void testHasPrivilege() {
        withPersistedAndAuthenticatedRestUser(feature::exportToCSCV);
    }

    @Test
    public void testExportsOnlyClubsWithContactPerson() {
        createClub();
        createClubWithMember(OccupationType.SEURAN_JASEN);
        final Occupation member = createClubWithMember(OccupationType.SEURAN_YHDYSHENKILO);

        withPersistedAndAuthenticatedRestUser(() -> {
            List<LHHuntingClubCSVRow> exportData = feature.exportToCSCV();
            assertEquals(1, exportData.size());
            assertMemberEqualsRow(member, exportData.get(0));
        });
    }

    @Test
    public void testDoNotExportTestClubs() {
        final HuntingClub club = createClub();
        club.setOfficialCode("8999999");
        final Occupation member1 = createContactPersonWithValidity(club, null, null, false);

        final HuntingClub club2 = createClub();
        club2.setOfficialCode("9999999");
        final Occupation member2 = createContactPersonWithValidity(club2, null, null, false);

        withPersistedAndAuthenticatedRestUser(() -> {
            List<LHHuntingClubCSVRow> exportData = feature.exportToCSCV();
            assertEquals(1, exportData.size());
            assertMemberEqualsRow(member1, exportData.get(0));
        });
    }

    @Test
    public void testExportsOnlyActiveContactPersons() {
        final HuntingClub club = createClub();

        final Occupation member1 = createContactPersonWithValidity(club, null, null, false);

        final LocalDate today = DateUtil.today();
        final Occupation member2 = createContactPersonWithValidity(club, today, null, false);
        final Occupation member3 = createContactPersonWithValidity(club, null, today, false);

        createContactPersonWithValidity(club, today.plusDays(1), null, false);
        createContactPersonWithValidity(club, null, today.minusDays(1), false);
        createContactPersonWithValidity(club, null, null, true);

        final HuntingClub clubMissingFromLhOrg = createClub();
        final Occupation member4 = createContactPersonWithValidity(clubMissingFromLhOrg, null, null, false);

        withPersistedAndAuthenticatedRestUser(() -> {
            List<LHHuntingClubCSVRow> exportData = feature.exportToCSCV();
            assertEquals(4, exportData.size());
            assertMemberEqualsRow(member1, exportData.get(0));
            assertMemberEqualsRow(member2, exportData.get(1));
            assertMemberEqualsRow(member3, exportData.get(2));
            assertMemberEqualsRow(member4, exportData.get(3));
        });
    }

    @Test
    public void testExportsAreSorted() {
        final HuntingClub club = createClub();

        final Occupation member1 = createContactPersonWithValidity(club, null, null, false);
        final Occupation member2 = createContactPersonWithValidity(club, null, null, false);
        final Occupation member3 = createContactPersonWithValidity(club, null, null, false);
        final Occupation member4 = createContactPersonWithValidity(club, null, null, false);

        member4.setCallOrder(0);
        member2.getPerson().setLastName("AA");
        member2.getPerson().setByName("AA");
        member1.getPerson().setLastName("AA");
        member1.getPerson().setByName("AB");

        final HuntingClub clubMissingFromLhOrg = createClub();
        final Occupation member5 = createContactPersonWithValidity(clubMissingFromLhOrg, null, null, false);

        withPersistedAndAuthenticatedRestUser(() -> {
            List<LHHuntingClubCSVRow> exportData = feature.exportToCSCV();
            assertEquals(5, exportData.size());
            assertMemberEqualsRow(member4, exportData.get(0));
            assertMemberEqualsRow(member2, exportData.get(1));
            assertMemberEqualsRow(member1, exportData.get(2));
            assertMemberEqualsRow(member3, exportData.get(3));
            assertMemberEqualsRow(member5, exportData.get(4));
        });
    }

    private static void assertMemberEqualsRow(Occupation member, LHHuntingClubCSVRow row) {
        final Organisation organisation = member.getOrganisation();
        assertEquals(organisation.getOfficialCode(), row.getAsiakasNumero());
        assertEquals(organisation.getParentOrganisation().getOfficialCode(), row.getRhy());
        assertEquals(organisation.getParentOrganisation().getParentOrganisation().getOfficialCode(), row.getValittuAlue());

        final Person person = member.getPerson();
        assertEquals(person.getSsn(), row.getHetu());
        assertEquals(person.getRhyMembership().getOfficialCode(), row.getRhy2());
        assertEquals(person.getRhyMembership().getParentOrganisation().getOfficialCode(), row.getValittuAlue2());
    }

    private Occupation createClubWithMember(OccupationType type) {
        final HuntingClub club = createClub();
        final Occupation member = model().newOccupation(club, personWithAddress(), type);
        //generate extra data to make sure sql queries do not multiply data
        model().newHuntingClubGroup(club);
        model().newOccupation(model().newHuntingClubGroup(club), personWithAddress(), OccupationType.RYHMAN_JASEN);
        model().newOccupation(model().newHuntingClubGroup(club), personWithAddress(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        return member;
    }

    private HuntingClub createClub() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(this.rka);
        final HuntingClub c = model().newHuntingClub(rhy);
        return c;
    }

    private Person personWithAddress() {
        final Riistanhoitoyhdistys rhyMembership = model().newRiistanhoitoyhdistys(this.rka);
        final Address mrAddress = model().newAddress();
        final Person p = model().newPerson();
        p.setRhyMembership(rhyMembership);
        p.setMrAddress(mrAddress);
        return p;
    }

    private Occupation createContactPersonWithValidity(final HuntingClub club,
                                                       final LocalDate beginDate,
                                                       final LocalDate endDate,
                                                       final boolean deleted) {
        final Occupation occupation = model().newOccupation(club, personWithAddress(), OccupationType.SEURAN_YHDYSHENKILO);
        occupation.setBeginDate(beginDate);
        occupation.setEndDate(endDate);
        if (deleted) {
            occupation.softDelete();
        }
        return occupation;
    }


    private void withPersistedAndAuthenticatedRestUser(Runnable cmd) {
        onSavedAndAuthenticated(createNewApiUser(SystemUserPrivilege.EXPORT_LUPAHALLINTA_HUNTINGCLUBS), cmd);
    }
}
