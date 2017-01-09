package fi.riista.feature.announcement.email;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class AnnouncementEmailResolverTest extends EmbeddedDatabaseTest {

    @Resource
    private AnnouncementEmailResolver clubAnnouncementEmailResolver;

    private void assertEmailList(final HuntingClub club,
                                 final EnumSet<OccupationType> occupationTypes,
                                 final String... expectedEmailList) {
        runInTransaction(() -> {
            final Set<String> emailList = clubAnnouncementEmailResolver.collectReceiverEmails(
                    entityManager().find(HuntingClub.class, club.getId()), occupationTypes);
            assertThat(emailList, hasSize(expectedEmailList.length));
            assertThat(emailList, containsInAnyOrder(expectedEmailList));
        });
    }

    @Test
    public void testSendAllGroupMembers() {
        final HuntingClub club = model().newHuntingClub();
        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        model().newOccupation(club, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person2, OccupationType.SEURAN_YHDYSHENKILO);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO);

        assertEmailList(club, targetOccupations, person1.getEmail(), person2.getEmail());
    }

    @Test
    public void testSendGroupLeaders() {
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

        assertEmailList(club, targetOccupations, person2.getEmail());
    }

    @Test
    public void testSendAllGroupMembers_FilterClub() {
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

        assertEmailList(club1, targetOccupations, person1.getEmail());
    }

    @Test
    public void testSendAllGroupMembers_DuplicateOccupations() {
        final HuntingClub club = model().newHuntingClub();
        final Person person = model().newPerson();
        model().newOccupation(club, person, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person, OccupationType.SEURAN_JASEN);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN);

        assertEmailList(club, targetOccupations, person.getEmail());
    }

    @Test
    public void testSendAllGroupMembers_IncludeContactPersons() {
        final HuntingClub club = model().newHuntingClub();
        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        model().newOccupation(club, person1, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, person2, OccupationType.SEURAN_YHDYSHENKILO);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.SEURAN_JASEN);

        assertEmailList(club, targetOccupations, person1.getEmail(), person2.getEmail());
    }

    @Test
    public void testSendGroupLeaders_DuplicateOccupations() {
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

        assertEmailList(club, targetOccupations, person.getEmail());
    }

    @Test
    public void testSendGroupLeaders_OnlyIfClubMember() {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        final Person person = model().newPerson();
        model().newOccupation(group, person, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        persistInNewTransaction();

        final EnumSet<OccupationType> targetOccupations = EnumSet.of(
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        assertEmailList(club, targetOccupations);
    }
}
