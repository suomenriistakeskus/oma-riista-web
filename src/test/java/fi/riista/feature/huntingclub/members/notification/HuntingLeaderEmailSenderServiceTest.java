package fi.riista.feature.huntingclub.members.notification;

import com.google.common.collect.Sets;
import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fi.riista.util.Asserts.assertEmpty;
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
    @Transactional
    public void testEmptyChanges() {
        assertEmpty(service.sendMails(Collections.emptyList()));
    }

    @Test
    @Transactional
    public void test() {
        final RhyAndCoordinator rhyAndCoordinator = createRhyAndCoordinator();

        final ClubAndContact clubAndContact = createClubAndContact(rhyAndCoordinator.rhy);
        final GroupAndMember groupAndMember1 = createGroupAndLeader(rhyAndCoordinator, clubAndContact);
        final GroupAndMember groupAndMember2 = createGroupAndLeader(rhyAndCoordinator, clubAndContact);

        persistInCurrentlyOpenTransaction();

        // changedLeaders has only one of the occupations as changed, we want all groups leaders even if one of the groups is changed
        final List<HuntingLeaderEmailSenderService.MailData> mailDatas = doSendMails(groupAndMember1);
        assertEquals(1, mailDatas.size());

        doAsserts(rhyAndCoordinator, clubAndContact, mailDatas, groupAndMember1, groupAndMember2);
    }

    @Test
    @Transactional
    public void testMailsAreSentByPermitRhy_oneClub() {
        final RhyAndCoordinator rhyAndCoordinator1 = createRhyAndCoordinator();
        final RhyAndCoordinator rhyAndCoordinator2 = createRhyAndCoordinator();

        final ClubAndContact clubAndContact = createClubAndContact(rhyAndCoordinator1.rhy);

        final GroupAndMember groupAndMember1 = createGroupAndLeader(rhyAndCoordinator1, clubAndContact);
        final GroupAndMember groupAndMember2 = createGroupAndLeader(rhyAndCoordinator1, clubAndContact);
        final GroupAndMember groupAndMember3 = createGroupAndLeader(rhyAndCoordinator2, clubAndContact);
        final GroupAndMember groupAndMember4 = createGroupAndLeader(rhyAndCoordinator2, clubAndContact);

        // Make sure that groups without permit are not sent
        final GroupAndMember groupAndMember5 = createGroupAndLeader(rhyAndCoordinator2, clubAndContact);
        groupAndMember5.group.updateHarvestPermit(null);

        persistInCurrentlyOpenTransaction();

        final List<HuntingLeaderEmailSenderService.MailData> mailDatas = doSendMails(groupAndMember1);
        assertEquals(2, mailDatas.size());

        doAsserts(rhyAndCoordinator1, clubAndContact, mailDatas, groupAndMember1, groupAndMember2);
        doAsserts(rhyAndCoordinator2, clubAndContact, mailDatas, groupAndMember3, groupAndMember4);
    }

    @Test
    @Transactional
    public void testMailsAreSentByPermitRhy_multipleClubs() {
        final RhyAndCoordinator rhyAndCoordinator1 = createRhyAndCoordinator();
        final RhyAndCoordinator rhyAndCoordinator2 = createRhyAndCoordinator();

        final ClubAndContact clubAndContact1 = createClubAndContact(rhyAndCoordinator1.rhy);
        final GroupAndMember groupAndMember1_1 = createGroupAndLeader(rhyAndCoordinator1, clubAndContact1);
        final GroupAndMember groupAndMember1_2 = createGroupAndLeader(rhyAndCoordinator1, clubAndContact1);

        final ClubAndContact clubAndContact2 = createClubAndContact(rhyAndCoordinator1.rhy);
        final GroupAndMember groupAndMember2_1 = createGroupAndLeader(rhyAndCoordinator2, clubAndContact2);
        final GroupAndMember groupAndMember2_2 = createGroupAndLeader(rhyAndCoordinator1, clubAndContact2);

        persistInCurrentlyOpenTransaction();

        final List<HuntingLeaderEmailSenderService.MailData> mailDatas = doSendMails(groupAndMember1_1, groupAndMember2_1);
        assertEquals(3, mailDatas.size());

        doAsserts(rhyAndCoordinator1, clubAndContact1, mailDatas, groupAndMember1_1, groupAndMember1_2);
        doAsserts(rhyAndCoordinator1, clubAndContact2, mailDatas, groupAndMember2_2);
        doAsserts(rhyAndCoordinator2, clubAndContact2, mailDatas, groupAndMember2_1);
    }

    private List<HuntingLeaderEmailSenderService.MailData> doSendMails(GroupAndMember... members) {
        return service.sendMails(Arrays.stream(members).map(m -> m.member).collect(toList()));
    }

    private static void doAsserts(final RhyAndCoordinator rhyAndCoordinator,
                                  final ClubAndContact clubAndContact,
                                  final List<HuntingLeaderEmailSenderService.MailData> data,
                                  final GroupAndMember... expectedGroupAndMembers) {

        final HuntingLeaderEmailSenderService.MailData res = findMailData(rhyAndCoordinator, clubAndContact, data);
        assertEmailAddresses(rhyAndCoordinator.coordinator, res, clubAndContact.club, clubAndContact.contact);

        assertEquals(expectedGroupAndMembers.length, res.groupRows.size());
        for (int i = 0; i < res.groupRows.size(); i++) {
            assertGroupAndRhy(res, res.groupRows.get(i), rhyAndCoordinator.rhy, expectedGroupAndMembers[i].group, expectedGroupAndMembers[i].member);
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
                                          final GroupEmailDto groupEmailDto,
                                          final Riistanhoitoyhdistys expectedRhy,
                                          final HuntingClubGroup expectedGroup,
                                          final Occupation expectedOccupation) {
        assertEquals(expectedRhy.getNameFinnish(), res.rhy.getNameFinnish());
        assertEquals(expectedGroup.getNameFinnish(), groupEmailDto.getNameFinnish());
        assertLeaders(groupEmailDto.getLeaders(), expectedOccupation);
    }

    private static void assertLeaders(final List<LeaderEmailDto> results, Occupation... expectedOccupations) {
        assertEquals(expectedOccupations.length, results.size());
        for (int i = 0; i < expectedOccupations.length; i++) {
            final Occupation expected = expectedOccupations[i];
            final LeaderEmailDto actual = results.get(i);
            assertEquals(expected.getCallOrder(), actual.getOrder());
            assertEquals(expected.getPerson().getFullName(), actual.getName());
            assertEquals(expected.getPerson().getHunterNumber(), actual.getHunterNumber());
        }
    }

    private static HuntingLeaderEmailSenderService.MailData findMailData(final RhyAndCoordinator rhyAndCoordinator,
                                                                         final ClubAndContact clubAndContact,
                                                                         final List<HuntingLeaderEmailSenderService.MailData> data) {
        final List<HuntingLeaderEmailSenderService.MailData> results = data.stream()
                .filter(d -> d.club.equals(clubAndContact.contact.getOrganisation()) && d.rhy.equals(rhyAndCoordinator.rhy))
                .collect(toList());
        assertEquals(1, results.size());
        return results.get(0);
    }

    private RhyAndCoordinator createRhyAndCoordinator() {
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys();
        final Person coordinator1 = model().newPerson();
        model().newOccupation(rhy1, coordinator1, OccupationType.TOIMINNANOHJAAJA);
        return new RhyAndCoordinator(rhy1, coordinator1);
    }

    private ClubAndContact createClubAndContact(Riistanhoitoyhdistys rhy) {
        final HuntingClub club = model().newHuntingClub(rhy);
        club.setNameFinnish(club.getNameFinnish() + counter);
        club.setNameSwedish(club.getNameSwedish() + counter);
        counter++;

        final Occupation occupation = model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO);
        return new ClubAndContact(club, occupation);
    }

    private GroupAndMember createGroupAndLeader(final RhyAndCoordinator rhy, final ClubAndContact club) {
        final HarvestPermit permit = model().newHarvestPermit(rhy.rhy);
        final HuntingClubGroup group = model().newHuntingClubGroup(club.club);
        group.setNameFinnish(group.getNameFinnish() + counter);
        group.setNameSwedish(group.getNameSwedish() + counter);
        group.updateHarvestPermit(permit);
        counter++;

        final Occupation occupation = model().newHuntingClubGroupMember(group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        occupation.setCallOrder(leaderCounter++);

        return new GroupAndMember(group, occupation);
    }

    private static class RhyAndCoordinator {
        final Riistanhoitoyhdistys rhy;
        final Person coordinator;

        RhyAndCoordinator(final Riistanhoitoyhdistys rhy, final Person coordinator) {
            this.rhy = rhy;
            this.coordinator = coordinator;
        }
    }

    private static class ClubAndContact {
        final HuntingClub club;
        final Occupation contact;

        ClubAndContact(HuntingClub club, Occupation contact) {
            this.club = club;
            this.contact = contact;
        }
    }

    private static class GroupAndMember {
        final HuntingClubGroup group;
        final Occupation member;

        GroupAndMember(HuntingClubGroup group, Occupation member) {
            this.group = group;
            this.member = member;
        }
    }
}
