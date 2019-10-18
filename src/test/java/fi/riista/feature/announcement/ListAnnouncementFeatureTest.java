package fi.riista.feature.announcement;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.announcement.show.ListAnnouncementDTO;
import fi.riista.feature.announcement.show.ListAnnouncementFeature;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Stream;

import static fi.riista.feature.announcement.AnnouncementMatcher.isEqualAnnouncement;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
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
        public final Person nobody;

        public ClubFixture(final EntitySupplier model, final Riistanhoitoyhdistys rhy) {
            club = model.newHuntingClub(rhy);
            group = model.newHuntingClubGroup(club);

            clubMember = model.newPerson();
            clubContact = model.newPerson();
            groupMember = model.newPerson();
            groupLeader = model.newPerson();
            nobody = model.newPerson();

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
        public final Person member;
        public final Person nobody;

        public RhyFixture(final EntitySupplier model, final Riistanhoitoyhdistys rhy) {
            this.rhy = rhy;
            coordinator = model.newPerson();
            srva = model.newPerson();
            valvoja = model.newPerson();
            member = model.newPerson();
            nobody = model.newPerson();

            model.newOccupation(rhy, coordinator, OccupationType.TOIMINNANOHJAAJA);
            model.newOccupation(rhy, srva, OccupationType.SRVA_YHTEYSHENKILO);
            model.newOccupation(rhy, valvoja, OccupationType.METSASTYKSENVALVOJA);

            member.setRhyMembership(rhy);
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

    private Announcement createPublicAnnouncement(final Organisation rk, final SystemUser moderator) {
        final Announcement announcement = model().newAnnouncement(moderator, rk, AnnouncementSenderType.RIISTAKESKUS);
        announcement.setVisibleToAll(true);
        return announcement;
    }

    private List<ListAnnouncementDTO> listReceived(final Organisation organisation) {
        return listAnnouncementFeature.listForOrganisation(
                organisation.getOrganisationType(),
                organisation.getOfficialCode(),
                new PageRequest(0, 1000)).getContent();
    }

    private List<ListAnnouncementDTO> listMine() {
        return listAnnouncementFeature.listMine(new PageRequest(0, 1000)).getContent();
    }

    private void assertNotVisible(final Person person) {
        onSavedAndAuthenticated(createUser(person), () -> {
            assertThat(listMine(), emptyCollectionOf(ListAnnouncementDTO.class));
        });
    }

    private void assertNotVisible(final Person person, final Organisation organisation) {
        onSavedAndAuthenticated(createUser(person), () -> {
            assertThat(listReceived(organisation), emptyCollectionOf(ListAnnouncementDTO.class));
        });
    }

    private void assertVisible(final Person person, final Announcement announcement) {
        onSavedAndAuthenticated(createUser(person), () -> {
            assertThat(listMine(), contains(isEqualAnnouncement(announcement)));
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

        // Club visibility
        assertVisible(f.clubMember, f.club, announcement);
        assertVisible(f.clubContact, f.club, announcement);
        assertVisible(f.groupMember, f.club, announcement);
        assertVisible(f.groupLeader, f.club, announcement);
        assertNotVisible(r.coordinator, f.club);
        assertNotVisible(r.member, f.club);
        assertNotVisible(r.srva, f.club);
        assertNotVisible(r.valvoja, f.club);
        assertNotVisible(f.nobody, f.club);

        // RHY visibility
        assertNotVisible(r.coordinator, r.rhy);
        assertNotVisible(r.member, r.rhy);
        assertNotVisible(f.nobody, r.rhy);

        // Personal visibility
        assertVisible(f.clubMember, announcement);
        assertVisible(f.clubContact, announcement);
        assertVisible(f.groupMember, announcement);
        assertVisible(f.groupLeader, announcement);
        assertNotVisible(r.coordinator);
        assertNotVisible(r.member);
        assertNotVisible(f.nobody);
    }

    @Test
    public void testClub_HuntingLeaders() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final RhyFixture r = new RhyFixture(model(), rhy);
        final ClubFixture f = new ClubFixture(model(), rhy);

        final Announcement announcement = createAnnouncement(f.club, f.club, createUser(f.clubContact),
                AnnouncementSenderType.SEURAN_YHDYSHENKILO, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        persistInNewTransaction();

        // Club visibility
        assertNotVisible(f.clubMember, f.club);
        assertVisible(f.clubContact, f.club, announcement);
        assertNotVisible(f.groupMember, f.club);
        assertVisible(f.groupLeader, f.club, announcement);
        assertNotVisible(r.coordinator, f.club);
        assertNotVisible(r.member, f.club);
        assertNotVisible(r.srva, f.club);
        assertNotVisible(r.valvoja, f.club);
        assertNotVisible(f.nobody, f.club);

        // RHY visibility
        assertNotVisible(r.coordinator, r.rhy);
        assertNotVisible(r.member, r.rhy);
        assertNotVisible(f.nobody, r.rhy);

        // Personal visibility
        assertNotVisible(f.clubMember);
        assertVisible(f.clubContact, announcement);
        assertNotVisible(f.groupMember);
        assertVisible(f.groupLeader, announcement);
        assertNotVisible(r.coordinator);
        assertNotVisible(r.member);
        assertNotVisible(f.nobody);
    }

    @Test
    public void testClub_GroupMembers() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final RhyFixture r = new RhyFixture(model(), rhy);
        final ClubFixture f = new ClubFixture(model(), rhy);

        final Announcement announcement = createAnnouncement(f.club, f.club, createUser(f.clubContact),
                AnnouncementSenderType.SEURAN_YHDYSHENKILO, OccupationType.RYHMAN_JASEN);

        persistInNewTransaction();

        // Club visibility
        assertNotVisible(f.clubMember, f.club);
        assertVisible(f.clubContact, f.club, announcement);
        assertVisible(f.groupMember, f.club, announcement);
        assertVisible(f.groupLeader, f.club, announcement);
        assertNotVisible(r.coordinator, f.club);
        assertNotVisible(r.member, f.club);
        assertNotVisible(r.srva, f.club);
        assertNotVisible(r.valvoja, f.club);
        assertNotVisible(f.nobody, f.club);

        // RHY visibility
        assertNotVisible(r.coordinator, r.rhy);
        assertNotVisible(r.member, r.rhy);
        assertNotVisible(r.srva, r.rhy);
        assertNotVisible(r.valvoja, r.rhy);
        assertNotVisible(f.nobody, r.rhy);

        // Personal visibility
        assertNotVisible(f.clubMember);
        assertVisible(f.clubContact, announcement);
        assertVisible(f.groupMember, announcement);
        assertVisible(f.groupLeader, announcement);
        assertNotVisible(r.coordinator);
        assertNotVisible(r.member);
        assertNotVisible(f.nobody);
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

        assertNotVisible(f1.nobody);
        assertNotVisible(f2.nobody);
    }

    @Test
    public void testCoordinator_AllClubMembers() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final RhyFixture r = new RhyFixture(model(), rhy);
        final ClubFixture f = new ClubFixture(model(), rhy);

        final Announcement announcement = createAnnouncement(r.rhy, r.rhy, createUser(r.coordinator),
                AnnouncementSenderType.TOIMINNANOHJAAJA, OccupationType.SEURAN_JASEN);

        persistInNewTransaction();

        // Club visibility
        assertVisible(f.clubMember, f.club, announcement);
        assertVisible(f.clubContact, f.club, announcement);
        assertVisible(f.groupMember, f.club, announcement);
        assertVisible(f.groupLeader, f.club, announcement);
        assertNotVisible(r.coordinator, f.club);
        assertNotVisible(r.member, f.club);
        assertNotVisible(r.srva, f.club);
        assertNotVisible(r.valvoja, f.club);
        assertNotVisible(f.nobody, f.club);

        // RHY visibility
        assertNotVisible(f.clubMember, r.rhy);
        assertNotVisible(f.clubContact, r.rhy);
        assertNotVisible(f.groupMember, r.rhy);
        assertNotVisible(f.groupLeader, r.rhy);
        assertNotVisible(f.nobody, r.rhy);
        assertVisible(r.coordinator, r.rhy, announcement);
        assertNotVisible(r.member, r.rhy);
        assertNotVisible(r.srva, r.rhy);
        assertNotVisible(r.valvoja, r.rhy);

        // Personal visibility
        assertVisible(f.clubMember, announcement);
        assertVisible(f.clubContact, announcement);
        assertVisible(f.groupMember, announcement);
        assertVisible(f.groupLeader, announcement);
        assertVisible(r.coordinator, announcement);
        assertNotVisible(r.member);
        assertNotVisible(f.nobody);
    }

    @Test
    public void testCoordinator_ClubIsNotChild() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys(rka);
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys(rka);

        final RhyFixture r1 = new RhyFixture(model(), rhy1);
        final RhyFixture r2 = new RhyFixture(model(), rhy2);
        final ClubFixture c1 = new ClubFixture(model(), rhy1);
        final ClubFixture c2 = new ClubFixture(model(), rhy2);

        final Announcement announcement = createAnnouncement(r1.rhy, r1.rhy, createUser(r1.coordinator),
                AnnouncementSenderType.TOIMINNANOHJAAJA, OccupationType.SEURAN_JASEN);

        persistInNewTransaction();

        // Club visibility
        assertVisible(c1.clubMember, c1.club, announcement);
        assertVisible(c1.clubContact, c1.club, announcement);
        assertVisible(c1.groupMember, c1.club, announcement);
        assertVisible(c1.groupLeader, c1.club, announcement);

        assertNotVisible(c2.clubMember, c2.club);
        assertNotVisible(c2.clubContact, c2.club);
        assertNotVisible(c2.groupMember, c2.club);
        assertNotVisible(c2.groupLeader, c2.club);

        // RHY visibility
        assertVisible(r1.coordinator, rhy1, announcement);
        assertVisible(r1.coordinator, announcement);
        assertNotVisible(r1.member, r1.rhy);

        assertNotVisible(r2.coordinator, rhy2);
        assertNotVisible(r2.member, r2.rhy);

        // Personal visibility
        assertVisible(c1.clubMember, announcement);
        assertVisible(c1.clubContact, announcement);
        assertVisible(c1.groupMember, announcement);
        assertVisible(c1.groupLeader, announcement);
        assertNotVisible(c1.nobody);

        assertNotVisible(c2.clubMember);
        assertNotVisible(c2.clubContact);
        assertNotVisible(c2.groupMember);
        assertNotVisible(c2.groupLeader);
        assertNotVisible(c2.nobody);

        assertVisible(r1.coordinator, announcement);
        assertNotVisible(r1.member);
        assertNotVisible(r1.srva);
        assertNotVisible(r1.valvoja);
        assertNotVisible(r1.nobody);

        assertNotVisible(r2.coordinator);
        assertNotVisible(r2.member);
        assertNotVisible(r2.srva);
        assertNotVisible(r2.valvoja);
        assertNotVisible(r2.nobody);
    }

    @Test
    public void testCoordinator_HuntingLeaders() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final RhyFixture r = new RhyFixture(model(), rhy);
        final ClubFixture c = new ClubFixture(model(), rhy);

        final Announcement announcement = createAnnouncement(r.rhy, r.rhy, createUser(r.coordinator),
                AnnouncementSenderType.TOIMINNANOHJAAJA, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        persistInNewTransaction();

        // Club visibility
        assertNotVisible(c.clubMember, c.club);
        assertVisible(c.clubContact, c.club, announcement);
        assertNotVisible(c.groupMember, c.club);
        assertVisible(c.groupLeader, c.club, announcement);
        assertNotVisible(c.nobody, c.club);
        assertNotVisible(r.coordinator, c.club);
        assertNotVisible(r.member, c.club);
        assertNotVisible(r.srva, c.club);
        assertNotVisible(r.valvoja, c.club);
        assertNotVisible(r.nobody, c.club);

        // RHY visibility
        assertVisible(r.coordinator, r.rhy, announcement);
        assertNotVisible(r.member, r.rhy);
        assertNotVisible(r.srva, r.rhy);
        assertNotVisible(r.valvoja, r.rhy);
        assertNotVisible(r.nobody, r.rhy);

        assertNotVisible(c.clubMember, r.rhy);
        assertNotVisible(c.clubContact, r.rhy);
        assertNotVisible(c.groupMember, r.rhy);
        assertNotVisible(c.groupLeader, r.rhy);

        // Personal visibility
        assertVisible(r.coordinator, announcement);
        assertNotVisible(r.member);
        assertNotVisible(r.srva);
        assertNotVisible(r.valvoja);
        assertNotVisible(r.nobody);

        assertNotVisible(c.clubMember);
        assertVisible(c.clubContact, announcement);
        assertNotVisible(c.groupMember);
        assertVisible(c.groupLeader, announcement);
    }

    @Test
    public void testCoordinator_RhyOccupation() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final RhyFixture r = new RhyFixture(model(), rhy);
        final ClubFixture c = new ClubFixture(model(), rhy);

        final Announcement announcement = createAnnouncement(rhy, rhy, createUser(r.coordinator),
                AnnouncementSenderType.TOIMINNANOHJAAJA, OccupationType.SRVA_YHTEYSHENKILO);

        persistInNewTransaction();

        // Club visibility
        assertNotVisible(r.coordinator, c.club);
        assertNotVisible(r.member, c.club);
        assertNotVisible(r.srva, c.club);
        assertNotVisible(r.valvoja, c.club);
        assertNotVisible(r.nobody, c.club);

        assertNotVisible(c.clubMember, c.club);
        assertNotVisible(c.clubContact, c.club);
        assertNotVisible(c.groupMember, c.club);
        assertNotVisible(c.groupLeader, c.club);
        assertNotVisible(c.nobody, c.club);

        // RHY visibility
        assertVisible(r.coordinator, r.rhy, announcement);
        assertVisible(r.srva, r.rhy, announcement);
        assertNotVisible(r.valvoja, r.rhy);
        assertNotVisible(r.member, r.rhy);
        assertNotVisible(r.nobody, r.rhy);

        assertNotVisible(c.clubMember, r.rhy);
        assertNotVisible(c.clubContact, r.rhy);
        assertNotVisible(c.groupMember, r.rhy);
        assertNotVisible(c.groupLeader, r.rhy);
        assertNotVisible(c.nobody, r.rhy);

        // Personal visibility
        assertVisible(r.coordinator, announcement);
        assertNotVisible(r.member);
        assertVisible(r.srva, announcement);
        assertNotVisible(r.valvoja);
        assertNotVisible(r.nobody);

        assertNotVisible(c.clubMember);
        assertNotVisible(c.clubContact);
        assertNotVisible(c.groupMember);
        assertNotVisible(c.groupLeader);
        assertNotVisible(c.nobody);
    }

    @Test
    public void testModerator_AllClubMembers() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Organisation rk = rka.getParentOrganisation();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);

        final SystemUser moderator = createNewModerator();

        final RhyFixture r = new RhyFixture(model(), rhy);
        final ClubFixture c1 = new ClubFixture(model(), rhy);
        final ClubFixture c2 = new ClubFixture(model(), rhy);

        final Announcement announcement = createAnnouncement(rk, rk, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SEURAN_JASEN);

        persistInNewTransaction();

        Stream.of(c1, c2).forEach(c -> {
            // Club visibility
            assertVisible(c.clubMember, c.club, announcement);
            assertVisible(c.clubContact, c.club, announcement);
            assertVisible(c.groupMember, c.club, announcement);
            assertVisible(c.groupLeader, c.club, announcement);
            assertNotVisible(c.nobody, c.club);

            // Personal visibility
            assertVisible(c.clubMember, announcement);
            assertVisible(c.clubContact, announcement);
            assertVisible(c.groupMember, announcement);
            assertVisible(c.groupLeader, announcement);
            assertNotVisible(c.nobody);
        });

        // RHY visibility
        assertVisible(r.coordinator, r.rhy, announcement);
        assertNotVisible(r.member, r.rhy);
        assertNotVisible(r.srva, r.rhy);
        assertNotVisible(r.valvoja, r.rhy);
        assertNotVisible(r.nobody, r.rhy);

        // Personal visibility
        assertVisible(r.coordinator, announcement);
        assertNotVisible(r.member);
        assertNotVisible(r.srva);
        assertNotVisible(r.valvoja);
        assertNotVisible(r.nobody);
    }

    @Test
    public void testModerator_HuntingLeaders() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Organisation rk = rka.getParentOrganisation();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);

        final SystemUser moderator = createNewModerator();

        final RhyFixture r = new RhyFixture(model(), rhy);
        final ClubFixture c1 = new ClubFixture(model(), rhy);
        final ClubFixture c2 = new ClubFixture(model(), rhy);

        final Announcement announcement = createAnnouncement(rk, rk, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        persistInNewTransaction();

        Stream.of(c1, c2).forEach(c -> {
            // Club visibility
            assertNotVisible(c.clubMember, c.club);
            assertVisible(c.clubContact, c.club, announcement);
            assertNotVisible(c.groupMember, c.club);
            assertVisible(c.groupLeader, c.club, announcement);
            assertNotVisible(c.nobody, c.club);

            // Personal visibility
            assertNotVisible(c.clubMember);
            assertVisible(c.clubContact, announcement);
            assertNotVisible(c.groupMember);
            assertVisible(c.groupLeader, announcement);
            assertNotVisible(c.nobody);
        });

        // RHY visibility
        assertVisible(r.coordinator, r.rhy, announcement);
        assertNotVisible(r.member, r.rhy);
        assertNotVisible(r.srva, r.rhy);
        assertNotVisible(r.valvoja, r.rhy);
        assertNotVisible(r.nobody, r.rhy);

        // Personal visibility
        assertVisible(r.coordinator, announcement);
        assertNotVisible(r.member);
        assertNotVisible(r.srva);
        assertNotVisible(r.valvoja);
        assertNotVisible(r.nobody);
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

        final Person clubAndRhyPerson = model().newPerson();
        model().newOccupation(c.club, clubAndRhyPerson, OccupationType.SEURAN_YHDYSHENKILO);
        model().newOccupation(r.rhy, clubAndRhyPerson, OccupationType.SRVA_YHTEYSHENKILO);

        persistInNewTransaction();

        // Club visibility

        assertNotVisible(c.clubMember, c.club);
        assertNotVisible(c.clubContact, c.club);
        assertNotVisible(c.groupMember, c.club);
        assertNotVisible(c.groupLeader, c.club);
        assertNotVisible(c.nobody, c.club);
        assertNotVisible(clubAndRhyPerson, c.club);

        // Rhy visibility

        assertVisible(r.coordinator, r.rhy, announcement);
        assertVisible(r.srva, r.rhy, announcement);
        assertNotVisible(r.valvoja, r.rhy);
        assertNotVisible(r.nobody, r.rhy);
        assertVisible(clubAndRhyPerson, r.rhy, announcement);

        // Personal visibility

        assertVisible(r.coordinator, announcement);
        assertNotVisible(r.member);
        assertVisible(r.srva, announcement);
        assertNotVisible(r.valvoja);
        assertNotVisible(r.nobody);
        assertVisible(clubAndRhyPerson, announcement);

        assertNotVisible(c.clubMember);
        assertNotVisible(c.clubContact);
        assertNotVisible(c.groupMember);
        assertNotVisible(c.groupLeader);
        assertNotVisible(c.nobody);
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

        assertNotVisible(r1.nobody);
        assertNotVisible(r1.member);

        // RKA 2
        assertNotVisible(r2.coordinator, r2.rhy);
        assertNotVisible(r2.coordinator);

        assertNotVisible(r2.srva, r2.rhy);
        assertNotVisible(r2.srva);

        assertNotVisible(r2.valvoja, r2.rhy);
        assertNotVisible(r2.valvoja);

        assertNotVisible(r2.nobody);
        assertNotVisible(r2.member);
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

        assertNotVisible(r1.nobody);
        assertNotVisible(r1.member);

        // RHY 2
        assertNotVisible(r2.coordinator, r2.rhy);
        assertNotVisible(r2.coordinator);

        assertNotVisible(r2.srva, r2.rhy);
        assertNotVisible(r2.srva);

        assertNotVisible(r2.valvoja, r2.rhy);
        assertNotVisible(r2.valvoja);

        assertNotVisible(r2.nobody);
        assertNotVisible(r2.member);
    }

    @Test
    public void testModerator() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Organisation rk = rka.getParentOrganisation();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);
        final HuntingClub club = model().newHuntingClub(rhy);

        final SystemUser moderator = createNewModerator();
        final SystemUser coordinator = createUserWithPerson();
        final SystemUser contactPerson = createUserWithPerson();

        // moderator: RK -> SRVA_YHTEYSHENKILO
        final Announcement moderator_srva_rk = createAnnouncement(rk, rk, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SRVA_YHTEYSHENKILO);
        final Announcement moderator_srva_rka = createAnnouncement(rk, rka, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SRVA_YHTEYSHENKILO);
        final Announcement moderator_srva_rhy = createAnnouncement(rk, rhy, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SRVA_YHTEYSHENKILO);

        // moderator: RK -> SEURAN_JASEN
        final Announcement moderator_club_member_rk = createAnnouncement(rk, rk, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SEURAN_JASEN);
        final Announcement moderator_club_member_rka = createAnnouncement(rk, rka, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SEURAN_JASEN);
        final Announcement moderator_club_member_rhy = createAnnouncement(rk, rhy, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SEURAN_JASEN);
        final Announcement moderator_club_member_club = createAnnouncement(rk, club, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.SEURAN_JASEN);

        // coordinator: RHY -> SRVA_YHTEYSHENKILO
        final Announcement coordinator_rhy_occupation = createAnnouncement(rhy, rhy, coordinator,
                AnnouncementSenderType.TOIMINNANOHJAAJA, OccupationType.SRVA_YHTEYSHENKILO);

        // coordinator: RHY -> members
        final Announcement coordinator_rhy_member = model().newAnnouncement(moderator, rhy,
                AnnouncementSenderType.TOIMINNANOHJAAJA);
        coordinator_rhy_member.setRhyMembershipSubscriber(rhy);

        // CLUB -> SEURAN_JASEN
        final Announcement clubcontact_club_members = createAnnouncement(club, club, contactPerson,
                AnnouncementSenderType.SEURAN_YHDYSHENKILO, OccupationType.SEURAN_JASEN);

        // CLUB -> METSASTYKSEN_JOHTAJA
        final Announcement clubcontact_club_hunting_leader = createAnnouncement(club, club, contactPerson,
                AnnouncementSenderType.SEURAN_YHDYSHENKILO, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        // Public announcement
        final Announcement visibleToAll = createPublicAnnouncement(rk, moderator);

        onSavedAndAuthenticated(moderator, () -> {
            assertThat(listReceived(rk), containsInAnyOrder(asList(
                    isEqualAnnouncement(moderator_srva_rk),
                    isEqualAnnouncement(moderator_srva_rka),
                    isEqualAnnouncement(moderator_srva_rhy),
                    isEqualAnnouncement(moderator_club_member_rk),
                    isEqualAnnouncement(moderator_club_member_rka),
                    isEqualAnnouncement(moderator_club_member_rhy),
                    isEqualAnnouncement(moderator_club_member_club),
                    isEqualAnnouncement(visibleToAll))));
            assertThat(listReceived(rka), containsInAnyOrder(asList(
                    isEqualAnnouncement(moderator_srva_rk),
                    isEqualAnnouncement(moderator_srva_rka),
                    isEqualAnnouncement(moderator_club_member_rk),
                    isEqualAnnouncement(moderator_club_member_rka))));
            assertThat(listReceived(rhy), containsInAnyOrder(asList(
                    isEqualAnnouncement(moderator_srva_rk),
                    isEqualAnnouncement(moderator_srva_rka),
                    isEqualAnnouncement(moderator_srva_rhy),
                    isEqualAnnouncement(moderator_club_member_rk),
                    isEqualAnnouncement(moderator_club_member_rka),
                    isEqualAnnouncement(moderator_club_member_rhy),
                    isEqualAnnouncement(coordinator_rhy_occupation),
                    isEqualAnnouncement(coordinator_rhy_member))));
            assertThat(listReceived(club), containsInAnyOrder(asList(
                    isEqualAnnouncement(moderator_club_member_rk),
                    isEqualAnnouncement(moderator_club_member_rka),
                    isEqualAnnouncement(moderator_club_member_rhy),
                    isEqualAnnouncement(moderator_club_member_club),
                    isEqualAnnouncement(clubcontact_club_members),
                    isEqualAnnouncement(clubcontact_club_hunting_leader))));
        });
    }

    @Test
    public void testModerator_ClubOccupationWithOtherOccupationTypes() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys(rka);

        final SystemUser moderator = createNewModerator();

        final RhyFixture r1 = new RhyFixture(model(), rhy1);

        // Additional club occupations
        final HuntingClub club = model().newHuntingClub(rhy1);
        model().newOccupation(club, r1.valvoja, OccupationType.SEURAN_YHDYSHENKILO);
        model().newOccupation(club, r1.srva, OccupationType.SEURAN_YHDYSHENKILO);
        model().newOccupation(club, r1.coordinator, OccupationType.SEURAN_YHDYSHENKILO);

        final Organisation rk = rka.getParentOrganisation();
        final Announcement announcement = createAnnouncement(rk, rka, moderator,
                AnnouncementSenderType.RIISTAKESKUS, OccupationType.METSASTYKSENVALVOJA);

        persistInNewTransaction();

        assertVisible(r1.coordinator, r1.rhy, announcement);
        assertVisible(r1.coordinator, announcement);

        assertNotVisible(r1.srva, r1.rhy);
        assertNotVisible(r1.srva);

        assertVisible(r1.valvoja, r1.rhy, announcement);
        assertVisible(r1.valvoja, announcement);
    }

    @Test
    public void testVisibleToAll() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Organisation rk = rka.getParentOrganisation();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);

        final SystemUser moderator = createNewModerator();

        final Announcement announcement = createPublicAnnouncement(rk, moderator);

        final RhyFixture r = new RhyFixture(model(), rhy);
        final ClubFixture c = new ClubFixture(model(), rhy);

        persistInNewTransaction();

        assertVisible(r.coordinator, announcement);
        assertVisible(r.srva, announcement);
        assertVisible(r.valvoja, announcement);
        assertVisible(c.clubMember, announcement);
        assertVisible(c.clubContact, announcement);
        assertVisible(c.groupMember, announcement);
        assertVisible(c.groupLeader, announcement);
        assertVisible(r.nobody, announcement);
        assertVisible(c.nobody, announcement);
        assertVisible(r.member, announcement);

        assertNotVisible(r.coordinator, r.rhy);
        assertNotVisible(r.srva, r.rhy);
        assertNotVisible(r.valvoja, r.rhy);
        assertNotVisible(c.clubMember, c.club);
        assertNotVisible(c.clubContact, c.club);
        assertNotVisible(c.groupMember, c.club);
        assertNotVisible(c.groupLeader, c.club);
        assertNotVisible(c.nobody, c.club);
        assertNotVisible(r.nobody, r.rhy);
        assertNotVisible(r.member, c.club);
        assertNotVisible(r.member, r.rhy);
    }

    @Test
    public void testRhyMembership_Smoke() {
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys();
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys();
        final RhyFixture r1 = new RhyFixture(model(), rhy1);
        final RhyFixture r2 = new RhyFixture(model(), rhy2);
        final ClubFixture c = new ClubFixture(model(), rhy1);

        final Announcement a1 = model().newAnnouncement(createUser(r1.coordinator), r1.rhy, AnnouncementSenderType.TOIMINNANOHJAAJA);
        a1.setRhyMembershipSubscriber(r1.rhy);

        final Announcement a2 = model().newAnnouncement(createUser(r2.coordinator), r2.rhy, AnnouncementSenderType.TOIMINNANOHJAAJA);
        a2.setRhyMembershipSubscriber(r2.rhy);

        persistInNewTransaction();

        assertNotVisible(c.clubMember, c.club);
        assertNotVisible(c.clubContact, c.club);
        assertNotVisible(c.groupMember, c.club);
        assertNotVisible(c.groupLeader, c.club);

        assertNotVisible(c.clubMember);
        assertNotVisible(c.clubContact);
        assertNotVisible(c.groupMember);
        assertNotVisible(c.groupLeader);

        assertVisible(r1.coordinator, r1.rhy, a1);
        assertVisible(r2.coordinator, r2.rhy, a2);

        assertNotVisible(c.nobody);
        assertNotVisible(r1.nobody);

        assertVisible(r1.member, a1);
        assertVisible(r2.member, a2);
    }
}
