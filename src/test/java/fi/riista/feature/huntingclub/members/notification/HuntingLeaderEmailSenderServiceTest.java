package fi.riista.feature.huntingclub.members.notification;

import com.google.common.collect.Sets;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.test.Asserts.assertEmpty;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class HuntingLeaderEmailSenderServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingLeaderEmailSenderService service;

    private int counter = 0;
    private int leaderCounter = 0;

    @Before
    public void setup() {
        this.counter = 0;
        this.leaderCounter = 0;
    }

    @Test
    public void testEmptyChanges() {
        assertEmpty(doSendMails());
    }

    @Test
    public void test() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final ClubAndContact clubAndContact = createClubAndContact(rhy);
            final GroupAndMember groupAndMember1 = createGroupAndLeader(rhy, clubAndContact.club);
            final GroupAndMember groupAndMember2 = createGroupAndLeader(rhy, clubAndContact.club);

            persistInNewTransaction();

            // changedLeaders has only one of the occupations as changed, we want all groups leaders even if one of the groups is changed
            final List<HuntingLeaderEmailSenderService.MailData> mailDatas = doSendMails(groupAndMember1);
            assertEquals(1, mailDatas.size());

            doAsserts(rhy, coordinator, clubAndContact, mailDatas, groupAndMember1, groupAndMember2);
        });
    }

    @Test
    public void testMailsAreSentByPermitRhy_oneClub() {
        withRhyAndCoordinator((rhy1, coordinator1) -> withRhyAndCoordinator((rhy2, coordinator2) -> {

            final ClubAndContact clubAndContact = createClubAndContact(rhy1);

            final GroupAndMember groupAndMember1 = createGroupAndLeader(rhy1, clubAndContact.club);
            final GroupAndMember groupAndMember2 = createGroupAndLeader(rhy1, clubAndContact.club);
            final GroupAndMember groupAndMember3 = createGroupAndLeader(rhy2, clubAndContact.club);
            final GroupAndMember groupAndMember4 = createGroupAndLeader(rhy2, clubAndContact.club);

            // Make sure that groups without permit are not sent
            final GroupAndMember groupAndMember5 = createGroupAndLeader(rhy2, clubAndContact.club);
            groupAndMember5.group.updateHarvestPermit(null);

            persistInNewTransaction();

            final List<HuntingLeaderEmailSenderService.MailData> mailDatas = doSendMails(groupAndMember1);
            assertEquals(2, mailDatas.size());

            doAsserts(rhy1, coordinator1, clubAndContact, mailDatas, groupAndMember1, groupAndMember2);
            doAsserts(rhy2, coordinator2, clubAndContact, mailDatas, groupAndMember3, groupAndMember4);
        }));
    }

    @Test
    public void testMailsAreSentByPermitRhy_multipleClubs() {
        withRhyAndCoordinator((rhy1, coordinator1) -> withRhyAndCoordinator((rhy2, coordinator2) -> {

            final ClubAndContact clubAndContact1 = createClubAndContact(rhy1);
            final GroupAndMember groupAndMember1_1 = createGroupAndLeader(rhy1, clubAndContact1.club);
            final GroupAndMember groupAndMember1_2 = createGroupAndLeader(rhy1, clubAndContact1.club);

            final ClubAndContact clubAndContact2 = createClubAndContact(rhy1);
            final GroupAndMember groupAndMember2_1 = createGroupAndLeader(rhy2, clubAndContact2.club);
            final GroupAndMember groupAndMember2_2 = createGroupAndLeader(rhy1, clubAndContact2.club);

            persistInNewTransaction();

            final List<HuntingLeaderEmailSenderService.MailData> mailDatas =
                    doSendMails(groupAndMember1_1, groupAndMember2_1);
            assertEquals(3, mailDatas.size());

            doAsserts(rhy1, coordinator1, clubAndContact1, mailDatas, groupAndMember1_1, groupAndMember1_2);
            doAsserts(rhy1, coordinator1, clubAndContact2, mailDatas, groupAndMember2_2);
            doAsserts(rhy2, coordinator2, clubAndContact2, mailDatas, groupAndMember2_1);
        }));
    }

    @Test
    public void testPastHuntingYearGroupsNotSent() {
        final int currentHuntingYear = DateUtil.huntingYear();

        withRhyAndCoordinator((rhy, coordinator) -> {
            final ClubAndContact clubAndContact = createClubAndContact(rhy);

            final GroupAndMember previousYear = createGroupAndLeader(rhy, clubAndContact.club);
            previousYear.group.setHuntingYear(currentHuntingYear - 1);

            final GroupAndMember currentYear = createGroupAndLeader(rhy, clubAndContact.club);
            currentYear.group.setHuntingYear(currentHuntingYear);

            final GroupAndMember nextYear = createGroupAndLeader(rhy, clubAndContact.club);
            nextYear.group.setHuntingYear(currentHuntingYear + 1);

            persistInNewTransaction();

            final List<HuntingLeaderEmailSenderService.MailData> mailDatas =
                    doSendMails(previousYear, currentYear, nextYear);
            assertEquals(1, mailDatas.size());

            doAsserts(rhy, coordinator, clubAndContact, mailDatas, nextYear, currentYear);
        });
    }

    private List<HuntingLeaderEmailSenderService.MailData> doSendMails(final GroupAndMember... members) {
        return callInTransaction(() -> service.sendMails(Arrays.stream(members).map(m -> m.member).collect(toList())));
    }

    private static void doAsserts(final Riistanhoitoyhdistys rhy,
                                  final Person coordinator,
                                  final ClubAndContact clubAndContact,
                                  final List<HuntingLeaderEmailSenderService.MailData> data,
                                  final GroupAndMember... expectedGroupAndMembers) {

        final HuntingLeaderEmailSenderService.MailData res = findMailData(rhy, clubAndContact, data);
        assertEmailAddresses(coordinator, res, clubAndContact.club, clubAndContact.contact);

        assertEquals(expectedGroupAndMembers.length, res.groupRows.size());
        for (int i = 0; i < res.groupRows.size(); i++) {
            assertGroupAndRhy(res, res.groupRows.get(i), rhy, expectedGroupAndMembers[i].group, expectedGroupAndMembers[i].member);
        }
    }

    private static void assertEmailAddresses(final Person coordinator,
                                             final HuntingLeaderEmailSenderService.MailData res,
                                             final HuntingClub expectedClub,
                                             final Occupation expectedOccupation) {

        assertEquals(expectedClub.getId(), res.club.getId());
        assertEquals(Sets.newHashSet(coordinator.getEmail(), expectedOccupation.getPerson().getEmail()), res.emailAddresses);
    }

    private static void assertGroupAndRhy(final HuntingLeaderEmailSenderService.MailData res,
                                          final GroupEmailDTO groupEmailDto,
                                          final Riistanhoitoyhdistys expectedRhy,
                                          final HuntingClubGroup expectedGroup,
                                          final Occupation expectedOccupation) {

        assertEquals(expectedRhy.getNameFinnish(), res.rhy.getNameFinnish());
        assertEquals(expectedGroup.getNameFinnish(), groupEmailDto.getNameFinnish());
        assertLeaders(groupEmailDto.getLeaders(), expectedOccupation);
    }

    private static void assertLeaders(final List<LeaderEmailDTO> results, final Occupation... expectedOccupations) {
        assertEquals(expectedOccupations.length, results.size());

        for (int i = 0; i < expectedOccupations.length; i++) {
            final Occupation expected = expectedOccupations[i];
            final LeaderEmailDTO actual = results.get(i);

            assertEquals(expected.getCallOrder(), actual.getOrder());
            assertEquals(expected.getPerson().getFullName(), actual.getName());
            assertEquals(expected.getPerson().getHunterNumber(), actual.getHunterNumber());
            assertEquals(LeaderEmailDTO.DATE_FORMAT.print(expected.getModificationTime().getTime()), actual.getDate());
            assertEquals(expected.getModificationTime(), actual.getDateForTests());
        }
    }

    private static HuntingLeaderEmailSenderService.MailData findMailData(final Riistanhoitoyhdistys rhy,
                                                                         final ClubAndContact clubAndContact,
                                                                         final List<HuntingLeaderEmailSenderService.MailData> data) {

        final List<HuntingLeaderEmailSenderService.MailData> results = data.stream()
                .filter(d -> d.club.equals(clubAndContact.contact.getOrganisation()) && d.rhy.equals(rhy))
                .collect(toList());
        assertEquals(1, results.size());
        return results.get(0);
    }

    private ClubAndContact createClubAndContact(final Riistanhoitoyhdistys rhy) {
        final HuntingClub club = model().newHuntingClub(rhy);
        club.setNameFinnish(club.getNameFinnish() + counter);
        club.setNameSwedish(club.getNameSwedish() + counter);
        counter++;

        final Occupation occupation = model().newHuntingClubMember(club, SEURAN_YHDYSHENKILO);
        return new ClubAndContact(club, occupation);
    }

    private GroupAndMember createGroupAndLeader(final Riistanhoitoyhdistys rhy, final HuntingClub club) {
        final HarvestPermit permit = model().newHarvestPermit(rhy);
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        group.setNameFinnish(group.getNameFinnish() + counter);
        group.setNameSwedish(group.getNameSwedish() + counter);
        group.updateHarvestPermit(permit);
        counter++;

        final Occupation occupation = model().newHuntingClubGroupMember(group, RYHMAN_METSASTYKSENJOHTAJA);
        occupation.setCallOrder(leaderCounter++);

        return new GroupAndMember(group, occupation);
    }

    private static class ClubAndContact {
        final HuntingClub club;
        final Occupation contact;

        ClubAndContact(final HuntingClub club, final Occupation contact) {
            this.club = club;
            this.contact = contact;
        }
    }

    private static class GroupAndMember {
        final HuntingClubGroup group;
        final Occupation member;

        GroupAndMember(final HuntingClubGroup group, final Occupation member) {
            this.group = group;
            this.member = member;
        }
    }
}
