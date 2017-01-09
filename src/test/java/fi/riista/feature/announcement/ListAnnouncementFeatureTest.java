package fi.riista.feature.announcement;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.announcement.show.ListAnnouncementDTO;
import fi.riista.feature.announcement.show.ListAnnouncementFeature;
import fi.riista.feature.announcement.show.ListAnnouncementRequest;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Stream;

import static fi.riista.feature.announcement.AnnouncementMatcher.isEqualAnnouncement;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.junit.Assert.assertThat;

public class ListAnnouncementFeatureTest extends EmbeddedDatabaseTest {

    static class ClubFixture {
        public final HuntingClub club;
        public final HuntingClubGroup group;
        public final Person clubMember;
        public final Person clubContact;
        public final Person groupMember;
        public final Person groupLeader;

        public ClubFixture(final EntitySupplier model, final Riistanhoitoyhdistys rhy) {
            club = model.newHuntingClub(rhy);
            group = model.newHuntingClubGroup(club);

            clubMember = model.newPerson();
            clubContact = model.newPerson();
            groupMember = model.newPerson();
            groupLeader = model.newPerson();

            model.newOccupation(club, clubMember, OccupationType.SEURAN_JASEN);
            model.newOccupation(club, groupMember, OccupationType.SEURAN_JASEN);
            model.newOccupation(club, groupLeader, OccupationType.SEURAN_JASEN);
            model.newOccupation(group, groupMember, OccupationType.RYHMAN_JASEN);
            model.newOccupation(club, clubContact, OccupationType.SEURAN_YHDYSHENKILO);
            model.newOccupation(group, groupLeader, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        }
    }

    static class RhyFixture {
        public final Riistanhoitoyhdistys rhy;
        public final Person coordinator;
        public final Person srva;
        public final Person valvoja;

        public RhyFixture(final EntitySupplier model, final Riistanhoitoyhdistys rhy) {
            this.rhy = rhy;
            coordinator = model.newPerson();
            srva = model.newPerson();
            valvoja = model.newPerson();

            model.newOccupation(rhy, coordinator, OccupationType.TOIMINNANOHJAAJA);
            model.newOccupation(rhy, srva, OccupationType.SRVA_YHTEYSHENKILO);
            model.newOccupation(rhy, valvoja, OccupationType.METSASTYKSENVALVOJA);
        }
    }

    @Resource
    private ListAnnouncementFeature listAnnouncementFeature;

    private Announcement createAnnouncement(final Organisation from,
                                            final Organisation to,
                                            final SystemUser fromUser,
                                            final AnnouncementSenderType senderType,
                                            final OccupationType subscriberOccupationType) {
        final Announcement announcement = model().newAnnouncement(fromUser, from, senderType);
        model().newAnnouncementSubscriber(announcement, to, subscriberOccupationType);
        return announcement;
    }

    private List<ListAnnouncementDTO> listReceived(final Organisation organisation) {
        return list(organisation, ListAnnouncementRequest.Direction.RECEIVED);
    }

    private List<ListAnnouncementDTO> listSent(final Organisation organisation) {
        return list(organisation, ListAnnouncementRequest.Direction.SENT);
    }

    private List<ListAnnouncementDTO> list(final Organisation organisation,
                                           final ListAnnouncementRequest.Direction direction) {
        final ListAnnouncementRequest request = new ListAnnouncementRequest();
        if (organisation != null) {
            request.setOrganisationType(organisation.getOrganisationType());
            request.setOfficialCode(organisation.getOfficialCode());
        }
        request.setDirection(direction);

        return listAnnouncementFeature.list(request, new PageRequest(0, 10)).getContent();
    }

    private void assertNotVisible(final Person person) {
        onSavedAndAuthenticated(createUser(person), () -> {
            assertThat(listReceived(null), emptyCollectionOf(ListAnnouncementDTO.class));
        });
    }

    private void assertNotVisible(final Person person, final Organisation organisation) {
        onSavedAndAuthenticated(createUser(person), () -> {
            assertThat(listReceived(organisation), emptyCollectionOf(ListAnnouncementDTO.class));
        });
    }

    private void assertVisible(final Person person, final Announcement announcement) {
        onSavedAndAuthenticated(createUser(person), () -> {
            assertThat(listReceived(null), contains(isEqualAnnouncement(announcement)));
        });
    }

    private void assertVisible(final Person person, final Organisation organisation, final Announcement announcement) {
        onSavedAndAuthenticated(createUser(person), () -> {
            assertThat(listReceived(organisation), contains(isEqualAnnouncement(announcement)));
        });
    }

    @Test
    public void testClub_AllMembers() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final RhyFixture r = new RhyFixture(model(), rhy);
        final ClubFixture f = new ClubFixture(model(), rhy);

        final Announcement announcement = createAnnouncement(f.club, f.club, createUser(f.clubContact),
                AnnouncementSenderType.SEURAN_YHDYSHENKILO, OccupationType.SEURAN_JASEN);

        persistInNewTransaction();

        assertVisible(f.clubMember, f.club, announcement);
        assertVisible(f.clubContact, f.club, announcement);
        assertVisible(f.groupMember, f.club, announcement);
        assertVisible(f.groupLeader, f.club, announcement);
        assertNotVisible(r.coordinator, r.rhy);
    }

    @Test
    public void testClub_HuntingLeaders() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final RhyFixture r = new RhyFixture(model(), rhy);
        final ClubFixture f = new ClubFixture(model(), rhy);

        final Announcement announcement = createAnnouncement(f.club, f.club, createUser(f.clubContact),
                AnnouncementSenderType.SEURAN_YHDYSHENKILO, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        persistInNewTransaction();

        assertNotVisible(f.clubMember, f.club);
        assertVisible(f.clubContact, f.club, announcement);
        assertNotVisible(f.groupMember, f.club);
        assertVisible(f.groupLeader, f.club, announcement);
        assertNotVisible(r.coordinator, r.rhy);
    }

    @Test
    public void testClub_MultipleClubs() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final ClubFixture f1 = new ClubFixture(model(), rhy);
        final ClubFixture f2 = new ClubFixture(model(), rhy);

        final Person clubMemberBoth = model().newPerson();
        model().newOccupation(f1.club, clubMemberBoth, OccupationType.SEURAN_JASEN);
        model().newOccupation(f2.club, clubMemberBoth, OccupationType.SEURAN_JASEN);

        final Announcement a1 = createAnnouncement(f1.club, f1.club, createUser(f1.clubContact),
                AnnouncementSenderType.SEURAN_YHDYSHENKILO, OccupationType.SEURAN_JASEN);
        final Announcement a2 = createAnnouncement(f2.club, f2.club, createUser(f2.clubContact),
                AnnouncementSenderType.SEURAN_YHDYSHENKILO, OccupationType.SEURAN_JASEN);

        persistInNewTransaction();

        assertVisible(f1.clubMember, f1.club, a1);
        assertVisible(f1.clubContact, f1.club, a1);
        assertVisible(f1.groupMember, f1.club, a1);
        assertVisible(f1.groupLeader, f1.club, a1);

        assertVisible(f2.clubMember, f2.club, a2);
        assertVisible(f2.clubContact, f2.club, a2);
        assertVisible(f2.groupMember, f2.club, a2);
        assertVisible(f2.groupLeader, f2.club, a2);

        assertVisible(clubMemberBoth, f1.club, a1);
        assertVisible(clubMemberBoth, f2.club, a2);
    }

    @Test
    public void testCoordinator_AllClubMembers() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final RhyFixture r = new RhyFixture(model(), rhy);
        final ClubFixture f = new ClubFixture(model(), rhy);

        final Announcement announcement = createAnnouncement(r.rhy, r.rhy, createUser(r.coordinator),
                AnnouncementSenderType.TOIMINNANOHJAAJA, OccupationType.SEURAN_JASEN);

        persistInNewTransaction();

        assertVisible(f.clubMember, f.club, announcement);
        assertVisible(f.clubContact, f.club, announcement);
        assertVisible(f.groupMember, f.club, announcement);
        assertVisible(f.groupLeader, f.club, announcement);
        assertVisible(r.coordinator, r.rhy, announcement);
    }

    @Test
    public void testCoordinator_ClubIsNotChild() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys(rka);
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys(rka);

        final RhyFixture r1 = new RhyFixture(model(), rhy1);
        final RhyFixture r2 = new RhyFixture(model(), rhy2);
        final ClubFixture f1 = new ClubFixture(model(), rhy1);
        final ClubFixture f2 = new ClubFixture(model(), rhy2);

        final Announcement announcement = createAnnouncement(r1.rhy, r1.rhy, createUser(r1.coordinator),
                AnnouncementSenderType.TOIMINNANOHJAAJA, OccupationType.SEURAN_JASEN);

        persistInNewTransaction();

        assertVisible(f1.clubMember, f1.club, announcement);
        assertVisible(f1.clubContact, f1.club, announcement);
        assertVisible(f1.groupMember, f1.club, announcement);
        assertVisible(f1.groupLeader, f1.club, announcement);

        assertNotVisible(f2.clubMember, f2.club);
        assertNotVisible(f2.clubContact, f2.club);
        assertNotVisible(f2.groupMember, f2.club);
        assertNotVisible(f2.groupLeader, f2.club);

        assertNotVisible(f2.clubMember);
        assertNotVisible(f2.clubContact);
        assertNotVisible(f2.groupMember);
        assertNotVisible(f2.groupLeader);

        assertVisible(r1.coordinator, rhy1, announcement);
        assertVisible(r1.coordinator, announcement);

        assertNotVisible(r2.coordinator, rhy2);
        assertNotVisible(r2.coordinator);
    }

    @Test
    public void testCoordinator_HuntingLeaders() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final RhyFixture r = new RhyFixture(model(), rhy);
        final ClubFixture f = new ClubFixture(model(), rhy);

        final Announcement announcement = createAnnouncement(r.rhy, r.rhy, createUser(r.coordinator),
                AnnouncementSenderType.TOIMINNANOHJAAJA, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        persistInNewTransaction();

        assertNotVisible(f.clubMember, f.club);
        assertVisible(f.clubContact, f.club, announcement);
        assertNotVisible(f.groupMember, f.club);
        assertVisible(f.groupLeader, f.club, announcement);
        assertVisible(r.coordinator, r.rhy, announcement);
    }

    @Test
    public void testCoordinator_RhyOccupation() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final RhyFixture r = new RhyFixture(model(), rhy);
        final ClubFixture c = new ClubFixture(model(), rhy);

        final Announcement announcement = createAnnouncement(rhy, rhy, createUser(r.coordinator),
                AnnouncementSenderType.TOIMINNANOHJAAJA, OccupationType.SRVA_YHTEYSHENKILO);

        persistInNewTransaction();

        assertVisible(r.srva, announcement);
        assertVisible(r.coordinator, announcement);
        assertNotVisible(r.valvoja);

        assertNotVisible(c.clubMember, c.club);
        assertNotVisible(c.clubContact, c.club);
        assertNotVisible(c.groupMember, c.club);
        assertNotVisible(c.groupLeader, c.club);

        assertNotVisible(c.clubMember);
        assertNotVisible(c.clubContact);
        assertNotVisible(c.groupMember);
        assertNotVisible(c.groupLeader);
    }

    @Test
    public void testModerator_AllClubMembers() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Organisation rk = rka.getParentOrganisation();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);

        final SystemUser moderator = createNewModerator();

        final RhyFixture r1 = new RhyFixture(model(), rhy);
        final ClubFixture c1 = new ClubFixture(model(), rhy);
        final ClubFixture c2 = new ClubFixture(model(), rhy);

        final Announcement announcement = createAnnouncement(rk, rk, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SEURAN_JASEN);

        persistInNewTransaction();

        Stream.of(c1, c2).forEach(c -> {
            assertVisible(c.clubMember, announcement);
            assertVisible(c.clubContact, announcement);
            assertVisible(c.groupMember, announcement);
            assertVisible(c.groupLeader, announcement);

            assertVisible(c.clubMember, c.club, announcement);
            assertVisible(c.clubContact, c.club, announcement);
            assertVisible(c.groupMember, c.club, announcement);
            assertVisible(c.groupLeader, c.club, announcement);
        });

        assertVisible(r1.coordinator, r1.rhy, announcement);

        assertVisible(r1.coordinator, announcement);
        assertNotVisible(r1.srva);
        assertNotVisible(r1.valvoja);
    }

    @Test
    public void testModerator_HuntingLeaders() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Organisation rk = rka.getParentOrganisation();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);

        final SystemUser moderator = createNewModerator();

        final RhyFixture r1 = new RhyFixture(model(), rhy);
        final ClubFixture c1 = new ClubFixture(model(), rhy);
        final ClubFixture c2 = new ClubFixture(model(), rhy);

        final Announcement announcement = createAnnouncement(rk, rk, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        persistInNewTransaction();

        Stream.of(c1, c2).forEach(c -> {
            assertNotVisible(c.clubMember);
            assertVisible(c.clubContact, announcement);
            assertNotVisible(c.groupMember);
            assertVisible(c.groupLeader, announcement);

            assertNotVisible(c.clubMember, c.club);
            assertVisible(c.clubContact, c.club, announcement);
            assertNotVisible(c.groupMember, c.club);
            assertVisible(c.groupLeader, c.club, announcement);
        });

        assertVisible(r1.coordinator, r1.rhy, announcement);

        assertVisible(r1.coordinator, announcement);
        assertNotVisible(r1.srva);
        assertNotVisible(r1.valvoja);
    }

    @Test
    public void testModerator_RhyOccupation() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Organisation rk = rka.getParentOrganisation();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);

        final SystemUser moderator = createNewModerator();

        final RhyFixture r = new RhyFixture(model(), rhy);
        final ClubFixture c = new ClubFixture(model(), rhy);

        final Announcement announcement = createAnnouncement(rk, rk, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SRVA_YHTEYSHENKILO);

        persistInNewTransaction();

        assertVisible(r.coordinator, r.rhy, announcement);
        assertVisible(r.coordinator, announcement);

        assertVisible(r.srva, r.rhy, announcement);
        assertVisible(r.srva, announcement);

        assertNotVisible(r.valvoja, r.rhy);
        assertNotVisible(r.valvoja);

        assertNotVisible(c.clubMember);
        assertNotVisible(c.clubContact);
        assertNotVisible(c.groupMember);
        assertNotVisible(c.groupLeader);

        assertNotVisible(c.clubMember, c.club);
        assertNotVisible(c.clubContact, c.club);
        assertNotVisible(c.groupMember, c.club);
        assertNotVisible(c.groupLeader, c.club);
    }

    @Test
    public void testModerator_RkaFilter() {
        final RiistakeskuksenAlue rka1 = model().newRiistakeskuksenAlue();
        final RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue();
        final Organisation rk = rka1.getParentOrganisation();
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys(rka1);
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys(rka2);

        final SystemUser moderator = createNewModerator();

        final RhyFixture r1 = new RhyFixture(model(), rhy1);
        final RhyFixture r2 = new RhyFixture(model(), rhy2);

        final Announcement announcement = createAnnouncement(rk, rka1, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SRVA_YHTEYSHENKILO);

        persistInNewTransaction();

        // RKA 1
        assertVisible(r1.coordinator, r1.rhy, announcement);
        assertVisible(r1.coordinator, announcement);

        assertVisible(r1.srva, r1.rhy, announcement);
        assertVisible(r1.srva, announcement);

        assertNotVisible(r1.valvoja, r1.rhy);
        assertNotVisible(r1.valvoja);

        // RKA 2
        assertNotVisible(r2.coordinator, r2.rhy);
        assertNotVisible(r2.coordinator);

        assertNotVisible(r2.srva, r2.rhy);
        assertNotVisible(r2.srva);

        assertNotVisible(r2.valvoja, r2.rhy);
        assertNotVisible(r2.valvoja);
    }

    @Test
    public void testModerator_RhyFilter() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Organisation rk = rka.getParentOrganisation();
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys(rka);
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys(rka);

        final SystemUser moderator = createNewModerator();

        final RhyFixture r1 = new RhyFixture(model(), rhy1);
        final RhyFixture r2 = new RhyFixture(model(), rhy2);

        final Announcement announcement = createAnnouncement(rk, rhy1, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SRVA_YHTEYSHENKILO);

        persistInNewTransaction();

        // RHY 1
        assertVisible(r1.coordinator, r1.rhy, announcement);
        assertVisible(r1.coordinator, announcement);

        assertVisible(r1.srva, r1.rhy, announcement);
        assertVisible(r1.srva, announcement);

        assertNotVisible(r1.valvoja, r1.rhy);
        assertNotVisible(r1.valvoja);

        // RHY 2
        assertNotVisible(r2.coordinator, r2.rhy);
        assertNotVisible(r2.coordinator);

        assertNotVisible(r2.srva, r2.rhy);
        assertNotVisible(r2.srva);

        assertNotVisible(r2.valvoja, r2.rhy);
        assertNotVisible(r2.valvoja);
    }

    @Test
    public void testModerator() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Organisation rk = rka.getParentOrganisation();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);
        final HuntingClub club = model().newHuntingClub(rhy);

        final SystemUser moderator = createNewModerator();

        // Subscriber has RHY occupation
        final Announcement a_rk = createAnnouncement(rk, rk, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SRVA_YHTEYSHENKILO);
        final Announcement a_rka = createAnnouncement(rk, rka, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SRVA_YHTEYSHENKILO);
        final Announcement a_rhy = createAnnouncement(rk, rhy, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SRVA_YHTEYSHENKILO);
        final Announcement a_club = createAnnouncement(rk, club, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SRVA_YHTEYSHENKILO);

        // Subscriber is CLUB
        final Announcement b_rk = createAnnouncement(rk, rk, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SEURAN_JASEN);
        final Announcement b_rka = createAnnouncement(rk, rka, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SEURAN_JASEN);
        final Announcement b_rhy = createAnnouncement(rk, rhy, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SEURAN_JASEN);
        final Announcement b_club = createAnnouncement(rk, club, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SEURAN_JASEN);

        onSavedAndAuthenticated(moderator, () -> {
            // Received
            assertThat(listReceived(rk), empty());
            assertThat(listReceived(rka), empty());
            assertThat(listReceived(rhy), containsInAnyOrder(asList(
                    isEqualAnnouncement(a_rk),
                    isEqualAnnouncement(a_rka),
                    isEqualAnnouncement(a_rhy))));
            assertThat(listReceived(club), containsInAnyOrder(asList(
                    isEqualAnnouncement(b_rk),
                    isEqualAnnouncement(b_rka),
                    isEqualAnnouncement(b_rhy),
                    isEqualAnnouncement(b_club))));

            // Sent
            assertThat(listSent(rk), containsInAnyOrder(asList(
                    isEqualAnnouncement(a_rk),
                    isEqualAnnouncement(a_rka),
                    isEqualAnnouncement(a_rhy),
                    isEqualAnnouncement(a_club),
                    isEqualAnnouncement(b_rk),
                    isEqualAnnouncement(b_rka),
                    isEqualAnnouncement(b_rhy),
                    isEqualAnnouncement(b_club))));
            assertThat(listSent(rka), empty());
            assertThat(listSent(rhy), empty());
            assertThat(listSent(club), empty());
        });
    }
}
