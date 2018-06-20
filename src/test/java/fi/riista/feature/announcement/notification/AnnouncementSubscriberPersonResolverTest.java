package fi.riista.feature.announcement.notification;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.announcement.AnnouncementSubscriber;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AnnouncementSubscriberPersonResolverTest extends EmbeddedDatabaseTest {

    @Resource
    private AnnouncementSubscriberPersonResolver announcementPushNotificationResolver;

    private static AnnouncementSubscriber createSubscriber(final Organisation subscriberOrganisation,
                                                           final OccupationType occupationType) {
        final AnnouncementSubscriber subscriber = new AnnouncementSubscriber();
        subscriber.setOrganisation(subscriberOrganisation);
        subscriber.setOccupationType(occupationType);
        return subscriber;
    }

    private void assertSubscribers(final List<AnnouncementSubscriber> subscribers,
                                   final Person... expectedPersons) {
        runInTransaction(() -> {
            final List<Long> personIds = announcementPushNotificationResolver.collectReceiverPersonIds(subscribers);
            final List<Long> expectedPersonIds = F.getNonNullIds(expectedPersons);
            assertEquals(ImmutableSet.copyOf(expectedPersonIds), ImmutableSet.copyOf(personIds));
        });
    }

    private void assertPersonIds(final Organisation subscriberOrganisation,
                                 final EnumSet<OccupationType> occupationTypes,
                                 final Person... expectedPersons) {
        final List<AnnouncementSubscriber> subscribers = new LinkedList<>();

        for (final OccupationType occupationType : occupationTypes) {
            subscribers.add(createSubscriber(subscriberOrganisation, occupationType));
        }

        assertSubscribers(subscribers, expectedPersons);
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
        model().newOccupation(rhy1, person1, OccupationType.TOIMINNANOHJAAJA);
        model().newOccupation(rhy1, person2, OccupationType.SRVA_YHTEYSHENKILO);
        model().newOccupation(rhy2, person3, OccupationType.TOIMINNANOHJAAJA);
        model().newOccupation(rhy2, person4, OccupationType.SRVA_YHTEYSHENKILO);
        model().newOccupation(club, person5, OccupationType.SEURAN_JASEN);

        persistInNewTransaction();

        final List<AnnouncementSubscriber> subscribers = new LinkedList<>();
        subscribers.add(createSubscriber(rhy1, OccupationType.TOIMINNANOHJAAJA));
        subscribers.add(createSubscriber(rka2, OccupationType.SRVA_YHTEYSHENKILO));
        subscribers.add(createSubscriber(rk, OccupationType.SEURAN_JASEN));

        assertSubscribers(subscribers, person1, person4, person5);
    }

    // CLUB -> CLUB

    @Test
    public void testClubToAllGroupMembers() {
        final HuntingClub club = model().newHuntingClub();
        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        model().newOccupation(club, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person2, OccupationType.SEURAN_YHDYSHENKILO);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO);

        assertPersonIds(club, targetOccupations, person1, person2);
    }

    @Test
    public void testClubToGroupLeaders() {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        model().newOccupation(club, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person2, OccupationType.SEURAN_JASEN);
        model().newOccupation(group, person1, OccupationType.RYHMAN_JASEN);
        model().newOccupation(group, person2, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        assertPersonIds(club, targetOccupations, person2);
    }

    @Test
    public void testClubToAllGroupMembers_FilterClub() {
        final HuntingClub club1 = model().newHuntingClub();
        final HuntingClub club2 = model().newHuntingClub();
        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        model().newOccupation(club1, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club2, person2, OccupationType.SEURAN_JASEN);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO);

        assertPersonIds(club1, targetOccupations, person1);
    }

    @Test
    public void testClubToAllGroupMembers_DuplicateOccupations() {
        final HuntingClub club = model().newHuntingClub();
        final Person person = model().newPerson();
        model().newOccupation(club, person, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person, OccupationType.SEURAN_JASEN);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN);

        assertPersonIds(club, targetOccupations, person);
    }

    @Test
    public void testClubToAllGroupMembers_IncludeContactPersons() {
        final HuntingClub club = model().newHuntingClub();
        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        model().newOccupation(club, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person2, OccupationType.SEURAN_YHDYSHENKILO);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN);

        assertPersonIds(club, targetOccupations, person1, person2);
    }

    @Test
    public void testClubToGroupLeaders_DuplicateOccupations() {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        final Person person = model().newPerson();
        model().newOccupation(club, person, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);
        model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);
        model().newOccupation(group, person, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        model().newOccupation(group, person, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        assertPersonIds(club, targetOccupations, person);
    }

    @Test
    public void testClubToGroupLeaders_OnlyIfClubMember() {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        final Person person = model().newPerson();
        model().newOccupation(group, person, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        assertPersonIds(club, targetOccupations);
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
        model().newOccupation(club1, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club1, person2, OccupationType.SEURAN_YHDYSHENKILO);
        model().newOccupation(club2, person3, OccupationType.SEURAN_JASEN);
        model().newOccupation(club2, person4, OccupationType.SEURAN_YHDYSHENKILO);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO);

        assertPersonIds(rhy1, targetOccupations, person1, person2);
        assertPersonIds(rhy2, targetOccupations, person3, person4);
    }

    @Test
    public void testRhyToGroupLeaders() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HuntingClub club = model().newHuntingClub(rhy);
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        model().newOccupation(club, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person2, OccupationType.SEURAN_JASEN);
        model().newOccupation(group, person1, OccupationType.RYHMAN_JASEN);
        model().newOccupation(group, person2, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        assertPersonIds(rhy, targetOccupations, person2);
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
        model().newOccupation(club1, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club1, person2, OccupationType.SEURAN_YHDYSHENKILO);
        model().newOccupation(club2, person3, OccupationType.SEURAN_JASEN);
        model().newOccupation(club2, person4, OccupationType.SEURAN_YHDYSHENKILO);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO);

        assertPersonIds(rka1, targetOccupations, person1, person2);
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
        model().newOccupation(club1, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club1, person2, OccupationType.SEURAN_YHDYSHENKILO);
        model().newOccupation(club2, person3, OccupationType.SEURAN_JASEN);
        model().newOccupation(club2, person4, OccupationType.SEURAN_YHDYSHENKILO);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO);

        assertPersonIds(rk, targetOccupations, person1, person2, person3, person4);
    }

    // RK -> RHY

    @Test
    public void testRkToRhyCoordinator() {
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
        model().newOccupation(rhy1, person1, OccupationType.TOIMINNANOHJAAJA);
        model().newOccupation(rhy1, person2, OccupationType.SRVA_YHTEYSHENKILO);
        model().newOccupation(rhy2, person3, OccupationType.TOIMINNANOHJAAJA);
        model().newOccupation(rhy2, person4, OccupationType.SRVA_YHTEYSHENKILO);
        model().newOccupation(club, person5, OccupationType.SEURAN_JASEN);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(OccupationType.TOIMINNANOHJAAJA);

        assertPersonIds(rk, targetOccupations, person1, person3);
    }
}
