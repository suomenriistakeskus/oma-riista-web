package fi.riista.feature.announcement;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.announcement.show.MobileAnnouncementDTO;
import fi.riista.feature.announcement.show.MobileAnnouncementFeature;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Resource;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class MobileAnnouncementFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MobileAnnouncementFeature mobileAnnouncementFeature;

    private Person createClubContactPerson(final HuntingClub club) {
        final Person contactPerson = model().newPerson();
        model().newOccupation(club, contactPerson, OccupationType.SEURAN_YHDYSHENKILO);
        return contactPerson;
    }

    private Person createClubAndGroupMember(final HuntingClub club, final HuntingClubGroup group) {
        final Person groupMember = model().newPerson();
        model().newOccupation(club, groupMember, OccupationType.SEURAN_JASEN);
        model().newOccupation(group, groupMember, OccupationType.RYHMAN_JASEN);
        return groupMember;
    }

    private Person createClubAndGroupHuntingLeader(final HuntingClub club, final HuntingClubGroup group) {
        final Person huntingLeader = model().newPerson();
        model().newOccupation(club, huntingLeader, OccupationType.SEURAN_JASEN);
        model().newOccupation(group, huntingLeader, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        return huntingLeader;
    }

    private Announcement createAnnouncementForClubOccupation(final HuntingClub club,
                                                             final SystemUser user,
                                                             final OccupationType subscriberOccupationType) {
        final Announcement announcement = model().newAnnouncement(user, club, AnnouncementSenderType.SEURAN_YHDYSHENKILO);
        model().newAnnouncementSubscriber(announcement, club, subscriberOccupationType);
        return announcement;
    }

    private List<MobileAnnouncementDTO> listAnnouncements() {
        return mobileAnnouncementFeature.listMobileAnnouncements(null, new PageRequest(0, 10));
    }

    @Test
    public void testClubContactPersonToMembers() {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);

        // Sender (conctact person)
        final Person contactPerson = createClubContactPerson(club);

        // Receiver (normal club and group member)
        final Person clubMember = createClubAndGroupMember(club, group);

        // Message
        final Announcement announcement = createAnnouncementForClubOccupation(
                club, createUser(contactPerson), OccupationType.SEURAN_JASEN);

        onSavedAndAuthenticated(createUser(clubMember), () -> {
            final List<MobileAnnouncementDTO> list = listAnnouncements();
            assertThat(list, hasSize(1));

            final MobileAnnouncementDTO dto = list.get(0);

            assertEquals(announcement.getBody(), dto.getBody());
            assertEquals(announcement.getSubject(), dto.getSubject());
            assertNotNull(dto.getSender());
            assertEquals(announcement.getFromOrganisation().getNameLocalisation().asMap(), dto.getSender().getOrganisation());
//            assertEquals(OccupationType.SEURAN_YHDYSHENKILO, dto.getSender().getTitle());
        });
    }

    @Test
    public void testClubContactPersonToMembers_onlyForHuntingLeaders() {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);

        // Sender (contact person)
        final Person contactPerson = createClubContactPerson(club);

        // Receiver (hunting leader)
        final Person huntingLeader = createClubAndGroupHuntingLeader(club, group);

        // Not receiver (group member)
        final Person clubMember = createClubAndGroupMember(club, group);

        // Message
        createAnnouncementForClubOccupation(
                club, createUser(contactPerson), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        onSavedAndAuthenticated(createUser(clubMember), () -> {
            assertThat("normal group member should not see", listAnnouncements(), hasSize(0));
        });

        onSavedAndAuthenticated(createUser(huntingLeader), () -> {
            assertThat("hunting leader should see", listAnnouncements(), hasSize(1));
        });
    }

    @Test
    public void testClubContactPersonToMembers_onlyForTargetClub() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HuntingClub club = model().newHuntingClub(rhy);
        final HuntingClubGroup group = model().newHuntingClubGroup(club);

        // Sender (contact person)
        final Person contactPerson = createClubContactPerson(club);

        // Receiver (normal club and group member)
        createClubAndGroupMember(club, group);

        // Message
        createAnnouncementForClubOccupation(
                club, createUser(contactPerson), OccupationType.SEURAN_JASEN);

        // Member of other huntingClub which is not the receiver
        final HuntingClub otherClub = model().newHuntingClub(rhy);
        final HuntingClubGroup otherGroup = model().newHuntingClubGroup(otherClub);
        final Person otherClubMember = createClubAndGroupMember(otherClub, otherGroup);

        onSavedAndAuthenticated(createUser(otherClubMember), () -> {
            assertThat("other club member should not see", listAnnouncements(), hasSize(0));
        });
    }

    @Test
    public void testClubContactPersonToMembers_multipleClubs() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HuntingClub club1 = model().newHuntingClub(rhy);
        final HuntingClub club2 = model().newHuntingClub(rhy);

        // Sender (contact person)
        final Person contactPerson1 = createClubContactPerson(club1);
        final Person contactPerson2 = createClubContactPerson(club2);

        // Receiver (normal club and group member)
        final Person person = model().newPerson();
        model().newOccupation(club1, person, OccupationType.SEURAN_JASEN);
        model().newOccupation(club2, person, OccupationType.SEURAN_JASEN);

        // Message
        final Announcement a1 = createAnnouncementForClubOccupation(club1, createUser(contactPerson1), OccupationType.SEURAN_JASEN);
        final Announcement a2 = createAnnouncementForClubOccupation(club2, createUser(contactPerson2), OccupationType.SEURAN_JASEN);

        onSavedAndAuthenticated(createUser(person), () -> {
            final List<MobileAnnouncementDTO> actual = listAnnouncements();

            assertThat(actual, hasSize(2));
            assertThat(actual, containsInAnyOrder(asList(
                    hasAnnouncement(a1),
                    hasAnnouncement(a2))));
        });
    }

    private static Matcher<MobileAnnouncementDTO> hasAnnouncement(final Announcement announcement) {
        return allOf(hasAnnouncementId(equalTo(announcement.getId())),
                hasAnnouncementBody(equalTo(announcement.getBody())),
                hasAnnouncementSubject(equalTo(announcement.getSubject())));
    }

    private static Matcher<MobileAnnouncementDTO> hasAnnouncementBody(Matcher<String> childMatcher) {
        return new FeatureMatcher<MobileAnnouncementDTO, String>(childMatcher, "body", "body") {
            @Override
            protected String featureValueOf(MobileAnnouncementDTO actual) {
                return actual.getBody();
            }
        };
    }

    private static Matcher<MobileAnnouncementDTO> hasAnnouncementSubject(Matcher<String> childMatcher) {
        return new FeatureMatcher<MobileAnnouncementDTO, String>(childMatcher, "subject", "subject") {
            @Override
            protected String featureValueOf(MobileAnnouncementDTO actual) {
                return actual.getSubject();
            }
        };
    }

    private static Matcher<MobileAnnouncementDTO> hasAnnouncementId(Matcher<Long> childMatcher) {
        return new FeatureMatcher<MobileAnnouncementDTO, Long>(childMatcher, "id", "id") {
            @Override
            protected Long featureValueOf(MobileAnnouncementDTO actual) {
                return actual.getId();
            }
        };
    }
}
