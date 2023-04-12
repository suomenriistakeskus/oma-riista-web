package fi.riista.feature.announcement.notification;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.announcement.AnnouncementSenderType;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.push.MobileClientDevice;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class AnnouncementSubscriberPersonResolverTest extends EmbeddedDatabaseTest {

    private static class TestSubscriber {
        private final Organisation subscriberOrganisation;
        private final OccupationType occupationType;

        private TestSubscriber(final Organisation subscriberOrganisation, final OccupationType occupationType) {
            this.subscriberOrganisation = Objects.requireNonNull(subscriberOrganisation);
            this.occupationType = Objects.requireNonNull(occupationType);
        }

        public Organisation getSubscriberOrganisation() {
            return subscriberOrganisation;
        }

        public OccupationType getOccupationType() {
            return occupationType;
        }
    }

    private static class TestReceiver {
        private final String email;
        private final String pushToken;

        public TestReceiver(final String email, final MobileClientDevice clientDevice) {
            this.email = email;
            this.pushToken = Objects.requireNonNull(clientDevice).getPushToken();
        }

        public String getEmail() {
            return email;
        }

        public String getPushToken() {
            return pushToken;
        }
    }

    @Resource
    private AnnouncementSubscriberPersonResolver announcementSubscriberPersonResolver;

    private Announcement createAnnouncement(final List<TestSubscriber> subscribers) {
        final SystemUser user = model().newUser(model().newPerson());
        final Riistanhoitoyhdistys from = model().newRiistanhoitoyhdistys();
        final Announcement announcement = model().newAnnouncement(user, from, AnnouncementSenderType.RIISTAKESKUS);

        for (TestSubscriber subscriber : subscribers) {
            model().newAnnouncementSubscriber(announcement, subscriber.getSubscriberOrganisation(), subscriber.getOccupationType());
        }

        return announcement;
    }

    private static List<TestSubscriber> createTestSubscribers(final Organisation subscriberOrganisation,
                                                              final EnumSet<OccupationType> occupationTypes) {
        final List<TestSubscriber> subscribers = new LinkedList<>();

        for (final OccupationType occupationType : occupationTypes) {
            subscribers.add(new TestSubscriber(subscriberOrganisation, occupationType));
        }

        return subscribers;
    }

    private void assertSubscribers(final Announcement announcement,
                                   final TestReceiver... expectedReceivers) {
        runInTransaction(() -> {
            final AnnouncementNotificationTargets targets = announcementSubscriberPersonResolver.collectTargets(announcement, true);

            final List<String> expectedEmails = F.mapNonNullsToList(expectedReceivers, TestReceiver::getEmail);
            final List<String> expectedPushTokens = F.mapNonNullsToList(expectedReceivers, TestReceiver::getPushToken);

            assertEquals(expectedEmails.size(), targets.getEmails().size());
            assertEquals(expectedPushTokens.size(), targets.getPushTokens().size());

            assertEquals(ImmutableSet.copyOf(expectedEmails), ImmutableSet.copyOf(targets.getEmails()));
            assertEquals(ImmutableSet.copyOf(expectedPushTokens), ImmutableSet.copyOf(targets.getPushTokens()));
        });
    }

    @Test
    public void testSmoke_MultipleSubscribers() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue();
        final Organisation rk = rka1.getParentOrganisation();
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys(rka1);
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys(rka2);
        final HuntingClub club = model().newHuntingClub(rhy1);
        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        final Person person3 = model().newPerson();
        final Person person4 = model().newPerson();
        final Person person5 = model().newPerson();
        createNewUser("user1", person1);
        createNewUser("user2", person2);
        createNewUser("user3", person3);
        createNewUser("user4", person4);
        createNewUser("user5", person5);

        final MobileClientDevice client1 = model().newMobileClientDevice(person1);
        final MobileClientDevice client2 = model().newMobileClientDevice(person2);
        final MobileClientDevice client3 = model().newMobileClientDevice(person3);
        final MobileClientDevice client4 = model().newMobileClientDevice(person4);
        final MobileClientDevice client5 = model().newMobileClientDevice(person5);

        model().newOccupation(rhy1, person1, OccupationType.TOIMINNANOHJAAJA);
        model().newOccupation(rhy1, person2, OccupationType.SRVA_YHTEYSHENKILO);
        model().newOccupation(rhy2, person3, OccupationType.TOIMINNANOHJAAJA);
        model().newOccupation(rhy2, person4, OccupationType.SRVA_YHTEYSHENKILO);
        model().newOccupation(club, person5, OccupationType.SEURAN_JASEN);

        final List<TestSubscriber> subscribers = new LinkedList<>();
        subscribers.add(new TestSubscriber(rhy1, OccupationType.TOIMINNANOHJAAJA));
        subscribers.add(new TestSubscriber(rka2, OccupationType.SRVA_YHTEYSHENKILO));
        subscribers.add(new TestSubscriber(rk, OccupationType.SEURAN_JASEN));

        final Announcement announcement = createAnnouncement(subscribers);

        persistInNewTransaction();

        assertSubscribers(announcement,
                new TestReceiver(rhy1.getEmail(), client1),
                new TestReceiver(person4.getEmail(), client4),
                new TestReceiver(person5.getEmail(), client5));
    }

    // CLUB -> CLUB

    @Test
    public void testClubToAllGroupMembers() {
        final HuntingClub club = model().newHuntingClub();
        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        createNewUser("user1", person1);
        createNewUser("user2", person2);
        final MobileClientDevice client1 = model().newMobileClientDevice(person1);
        final MobileClientDevice client2 = model().newMobileClientDevice(person2);

        model().newOccupation(club, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person2, OccupationType.SEURAN_YHDYSHENKILO);

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO);

        final Announcement announcement = createAnnouncement(createTestSubscribers(club, targetOccupations));

        persistInNewTransaction();

        assertSubscribers(announcement, new TestReceiver(person1.getEmail(), client1), new TestReceiver(person2.getEmail(), client2));
    }

    @Test
    public void testClubToGroupLeaders() {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);

        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        createNewUser("user1", person1);
        createNewUser("user2", person2);
        final MobileClientDevice client1 = model().newMobileClientDevice(person1);
        final MobileClientDevice client2 = model().newMobileClientDevice(person2);

        model().newOccupation(club, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person2, OccupationType.SEURAN_JASEN);
        model().newOccupation(group, person1, OccupationType.RYHMAN_JASEN);
        model().newOccupation(group, person2, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        final Announcement announcement = createAnnouncement(createTestSubscribers(club, targetOccupations));

        persistInNewTransaction();

        assertSubscribers(announcement, new TestReceiver(person2.getEmail(), client2));
    }

    @Test
    public void testClubToAllGroupMembers_FilterClub() {
        final HuntingClub club1 = model().newHuntingClub();
        final HuntingClub club2 = model().newHuntingClub();

        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        createNewUser("user1", person1);
        createNewUser("user2", person2);
        final MobileClientDevice client1 = model().newMobileClientDevice(person1);
        final MobileClientDevice client2 = model().newMobileClientDevice(person2);

        model().newOccupation(club1, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club2, person2, OccupationType.SEURAN_JASEN);

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO);

        final Announcement announcement = createAnnouncement(createTestSubscribers(club1, targetOccupations));

        persistInNewTransaction();

        assertSubscribers(announcement, new TestReceiver(person1.getEmail(), client1));
    }

    @Test
    public void testClubToAllGroupMembers_DuplicateOccupations() {
        final HuntingClub club = model().newHuntingClub();
        final Person person = model().newPerson();
        createNewUser("user1", person);
        final MobileClientDevice client = model().newMobileClientDevice(person);

        model().newOccupation(club, person, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person, OccupationType.SEURAN_JASEN);

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN);

        final Announcement announcement = createAnnouncement(createTestSubscribers(club, targetOccupations));

        persistInNewTransaction();

        assertSubscribers(announcement, new TestReceiver(person.getEmail(), client));
    }

    @Test
    public void testClubToAllGroupMembers_IncludeContactPersons() {
        final HuntingClub club = model().newHuntingClub();
        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        createNewUser("user1", person1);
        createNewUser("user2", person2);
        final MobileClientDevice client1 = model().newMobileClientDevice(person1);
        final MobileClientDevice client2 = model().newMobileClientDevice(person2);

        model().newOccupation(club, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person2, OccupationType.SEURAN_YHDYSHENKILO);

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN);

        final Announcement announcement = createAnnouncement(createTestSubscribers(club, targetOccupations));

        persistInNewTransaction();

        assertSubscribers(announcement, new TestReceiver(person1.getEmail(), client1), new TestReceiver(person2.getEmail(), client2));
    }

    @Test
    public void testClubToGroupLeaders_DuplicateOccupations() {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);

        final Person person = model().newPerson();
        createNewUser("user1", person);
        final MobileClientDevice client = model().newMobileClientDevice(person);

        model().newOccupation(club, person, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);
        model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);
        model().newOccupation(group, person, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        model().newOccupation(group, person, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        final Announcement announcement = createAnnouncement(createTestSubscribers(club, targetOccupations));

        persistInNewTransaction();

        assertSubscribers(announcement, new TestReceiver(person.getEmail(), client));
    }

    @Test
    public void testClubToGroupLeaders_OnlyIfClubMember() {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);

        final Person person = model().newPerson();

        final MobileClientDevice client = model().newMobileClientDevice(person);

        model().newOccupation(group, person, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        final Announcement announcement = createAnnouncement(createTestSubscribers(club, targetOccupations));

        persistInNewTransaction();

        assertSubscribers(announcement);
    }

    // RHY -> CLUB

    @Test
    public void testRhyToClubMembers() {
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys();
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys();
        final HuntingClub club1 = model().newHuntingClub(rhy1);
        final HuntingClub club2 = model().newHuntingClub(rhy2);

        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        final Person person3 = model().newPerson();
        final Person person4 = model().newPerson();
        createNewUser("user1", person1);
        createNewUser("user2", person2);
        createNewUser("user3", person3);
        createNewUser("user4", person4);
        final MobileClientDevice client1 = model().newMobileClientDevice(person1);
        final MobileClientDevice client2 = model().newMobileClientDevice(person2);
        final MobileClientDevice client3 = model().newMobileClientDevice(person3);
        final MobileClientDevice client4 = model().newMobileClientDevice(person4);

        model().newOccupation(club1, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club1, person2, OccupationType.SEURAN_YHDYSHENKILO);
        model().newOccupation(club2, person3, OccupationType.SEURAN_JASEN);
        model().newOccupation(club2, person4, OccupationType.SEURAN_YHDYSHENKILO);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO);

        final Announcement announcement1 = createAnnouncement(createTestSubscribers(rhy1, targetOccupations));
        final Announcement announcement2 = createAnnouncement(createTestSubscribers(rhy2, targetOccupations));

        persistInNewTransaction();

        assertSubscribers(announcement1, new TestReceiver(person1.getEmail(), client1), new TestReceiver(person2.getEmail(), client2));
        assertSubscribers(announcement2, new TestReceiver(person3.getEmail(), client3), new TestReceiver(person4.getEmail(), client4));
    }

    @Test
    public void testRhyToGroupLeaders() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HuntingClub club = model().newHuntingClub(rhy);
        final HuntingClubGroup group = model().newHuntingClubGroup(club);

        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        createNewUser("user1", person1);
        createNewUser("user2", person2);
        final MobileClientDevice client1 = model().newMobileClientDevice(person1);
        final MobileClientDevice client2 = model().newMobileClientDevice(person2);

        model().newOccupation(club, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person2, OccupationType.SEURAN_JASEN);
        model().newOccupation(group, person1, OccupationType.RYHMAN_JASEN);
        model().newOccupation(group, person2, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        final Announcement announcement = createAnnouncement(createTestSubscribers(rhy, targetOccupations));

        persistInNewTransaction();

        assertSubscribers(announcement, new TestReceiver(person2.getEmail(), client2));
    }

    // RKA -> CLUB

    @Test
    public void testRkaToClubMembers() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys(rka1);
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys(rka2);
        final HuntingClub club1 = model().newHuntingClub(rhy1);
        final HuntingClub club2 = model().newHuntingClub(rhy2);

        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        final Person person3 = model().newPerson();
        final Person person4 = model().newPerson();
        createNewUser("user1", person1);
        createNewUser("user2", person2);
        createNewUser("user3", person3);
        createNewUser("user4", person4);
        final MobileClientDevice client1 = model().newMobileClientDevice(person1);
        final MobileClientDevice client2 = model().newMobileClientDevice(person2);
        final MobileClientDevice client3 = model().newMobileClientDevice(person3);
        final MobileClientDevice client4 = model().newMobileClientDevice(person4);

        model().newOccupation(club1, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club1, person2, OccupationType.SEURAN_YHDYSHENKILO);
        model().newOccupation(club2, person3, OccupationType.SEURAN_JASEN);
        model().newOccupation(club2, person4, OccupationType.SEURAN_YHDYSHENKILO);

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO);

        final Announcement announcement = createAnnouncement(createTestSubscribers(rka1, targetOccupations));

        persistInNewTransaction();

        assertSubscribers(announcement, new TestReceiver(person1.getEmail(), client1), new TestReceiver(person2.getEmail(), client2));
    }

    // RK -> CLUB

    @Test
    public void testRkToClubMembers() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue();
        final Organisation rk = rka1.getParentOrganisation();
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys(rka1);
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys(rka2);
        final HuntingClub club1 = model().newHuntingClub(rhy1);
        final HuntingClub club2 = model().newHuntingClub(rhy2);

        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        final Person person3 = model().newPerson();
        final Person person4 = model().newPerson();
        createNewUser("user1", person1);
        createNewUser("user2", person2);
        createNewUser("user3", person3);
        createNewUser("user4", person4);
        final MobileClientDevice client1 = model().newMobileClientDevice(person1);
        final MobileClientDevice client2 = model().newMobileClientDevice(person2);
        final MobileClientDevice client3 = model().newMobileClientDevice(person3);
        final MobileClientDevice client4 = model().newMobileClientDevice(person4);

        model().newOccupation(club1, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club1, person2, OccupationType.SEURAN_YHDYSHENKILO);
        model().newOccupation(club2, person3, OccupationType.SEURAN_JASEN);
        model().newOccupation(club2, person4, OccupationType.SEURAN_YHDYSHENKILO);

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO);

        final Announcement announcement = createAnnouncement(createTestSubscribers(rk, targetOccupations));

        persistInNewTransaction();

        assertSubscribers(announcement, new TestReceiver(person1.getEmail(), client1), new TestReceiver(person2.getEmail(), client2), new TestReceiver(person3.getEmail(), client3), new TestReceiver(person4.getEmail(), client4));
    }

    // RK -> RHY

    @Test
    public void testRkToRhyCoordinator() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue();
        final Organisation rk = rka1.getParentOrganisation();
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys(rka1);
        rhy1.setEmail("rhy1-email@invalid.com");
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys(rka2);
        rhy2.setEmail("rhy2-email@invalid.com");
        final Riistanhoitoyhdistys rhy3 = model().newRiistanhoitoyhdistys(rka2);
        rhy3.setEmail("rhy3-email@invalid.com");
        final HuntingClub club = model().newHuntingClub(rhy1);

        final Person person1 = model().newPerson(rhy1);
        final Person person2 = model().newPerson(rhy1);
        final Person person3 = model().newPerson(rhy2);
        final Person person4 = model().newPerson(rhy2);
        final Person person5 = model().newPerson(rhy3);

        final MobileClientDevice client1 = model().newMobileClientDevice(person1);
        final MobileClientDevice client2 = model().newMobileClientDevice(person2);
        final MobileClientDevice client3 = model().newMobileClientDevice(person3);
        final MobileClientDevice client4 = model().newMobileClientDevice(person4);
        final MobileClientDevice client5 = model().newMobileClientDevice(person5);

        model().newOccupation(rhy1, person1, OccupationType.TOIMINNANOHJAAJA);
        model().newOccupation(rhy1, person2, OccupationType.SRVA_YHTEYSHENKILO);
        model().newOccupation(rhy2, person3, OccupationType.TOIMINNANOHJAAJA);
        model().newOccupation(rhy2, person4, OccupationType.SRVA_YHTEYSHENKILO);
        model().newOccupation(club, person5, OccupationType.SEURAN_JASEN);

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(OccupationType.TOIMINNANOHJAAJA);

        final Announcement announcement = createAnnouncement(createTestSubscribers(rk, targetOccupations));

        persistInNewTransaction();

        assertSubscribers(announcement, new TestReceiver(rhy1.getEmail(), client1), new TestReceiver(rhy2.getEmail(), client3));
    }

    @Test
    public void testRkToRhyCoordinator_fallbackCoordinatorEmail() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue();
        final Organisation rk = rka1.getParentOrganisation();
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys(rka1);
        rhy1.setEmail("rhy1-email@invalid.com");
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys(rka2);
        final Riistanhoitoyhdistys rhy3 = model().newRiistanhoitoyhdistys(rka2);
        rhy3.setEmail("rhy3-email@invalid.com");
        final HuntingClub club = model().newHuntingClub(rhy1);

        final Person person1 = model().newPerson(rhy1);
        final Person person2 = model().newPerson(rhy1);
        final Person person3 = model().newPerson(rhy2);
        final Person person4 = model().newPerson(rhy2);
        final Person person5 = model().newPerson(rhy3);

        final MobileClientDevice client1 = model().newMobileClientDevice(person1);
        final MobileClientDevice client2 = model().newMobileClientDevice(person2);
        final MobileClientDevice client3 = model().newMobileClientDevice(person3);
        final MobileClientDevice client4 = model().newMobileClientDevice(person4);
        final MobileClientDevice client5 = model().newMobileClientDevice(person5);

        model().newOccupation(rhy1, person1, OccupationType.TOIMINNANOHJAAJA);
        model().newOccupation(rhy1, person2, OccupationType.SRVA_YHTEYSHENKILO);
        model().newOccupation(rhy2, person3, OccupationType.TOIMINNANOHJAAJA);
        model().newOccupation(rhy2, person4, OccupationType.SRVA_YHTEYSHENKILO);
        model().newOccupation(club, person5, OccupationType.SEURAN_JASEN);

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(OccupationType.TOIMINNANOHJAAJA);

        final Announcement announcement = createAnnouncement(createTestSubscribers(rk, targetOccupations));

        persistInNewTransaction();

        assertSubscribers(announcement, new TestReceiver(rhy1.getEmail(), client1), new TestReceiver(person3.getEmail(), client3));
    }

    // RHY -> RHY members

    @Test
    public void testRhyToMembers() {
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys();
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys();

        final Person person1 = model().newPerson();
        person1.setRhyMembership(rhy1);
        final Person person2 = model().newPerson();
        person2.setRhyMembership(rhy2);
        final Person person3 = model().newPerson();
        createNewUser("user1", person1);
        createNewUser("user2", person2);
        createNewUser("user3", person3);
        final MobileClientDevice client1 = model().newMobileClientDevice(person1);
        final MobileClientDevice client2 = model().newMobileClientDevice(person2);
        final MobileClientDevice client3 = model().newMobileClientDevice(person3);

        final SystemUser user = model().newUser(model().newPerson());
        final Riistanhoitoyhdistys from = model().newRiistanhoitoyhdistys();
        final Announcement announcement = model().newAnnouncement(user, from, AnnouncementSenderType.RIISTAKESKUS);
        announcement.setRhyMembershipSubscriber(rhy1);

        persistInNewTransaction();

        assertSubscribers(announcement, new TestReceiver(person1.getEmail(), client1));
    }

    @Test
    public void testRhyToMembers_noEmailToUnregisteredMember() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final Person registeredPerson1 = model().newPerson();
        registeredPerson1.setRhyMembership(rhy);
        final Person unregisteredPerson = model().newPerson();
        unregisteredPerson.setRhyMembership(rhy);
        createNewUser("user1", registeredPerson1);

        final MobileClientDevice client1 = model().newMobileClientDevice(registeredPerson1);

        final SystemUser user = model().newUser(model().newPerson());
        final Riistanhoitoyhdistys from = model().newRiistanhoitoyhdistys();
        final Announcement announcement = model().newAnnouncement(user, from, AnnouncementSenderType.RIISTAKESKUS);
        announcement.setRhyMembershipSubscriber(rhy);

        persistInNewTransaction();

        assertSubscribers(announcement, new TestReceiver(registeredPerson1.getEmail(), client1));
    }

    @Test
    public void testPersonDeniedAnnouncementEmail() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys(rka1);
        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        person2.setDenyAnnouncementEmail(true);

        createNewUser("user1", person1);
        createNewUser("user2", person2);

        final MobileClientDevice client1 = model().newMobileClientDevice(person1);
        final MobileClientDevice client2 = model().newMobileClientDevice(person2);

        model().newOccupation(rhy1, person1, OccupationType.PUHEENJOHTAJA);
        model().newOccupation(rhy1, person2, OccupationType.SRVA_YHTEYSHENKILO);

        final List<TestSubscriber> subscribers = new LinkedList<>();
        subscribers.add(new TestSubscriber(rka1, OccupationType.PUHEENJOHTAJA));
        subscribers.add(new TestSubscriber(rka1, OccupationType.SRVA_YHTEYSHENKILO));

        final Announcement announcement = createAnnouncement(subscribers);

        persistInNewTransaction();

        assertSubscribers(announcement, new TestReceiver(person1.getEmail(), client1), new TestReceiver(null, client2));
    }
}
