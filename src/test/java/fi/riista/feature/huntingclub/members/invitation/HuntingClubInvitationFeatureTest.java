package fi.riista.feature.huntingclub.members.invitation;

import com.google.common.collect.Sets;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.members.AbstractClubSpecificOccupationCrudFeatureTest;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.Occupation_;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSpecs;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

public class HuntingClubInvitationFeatureTest extends AbstractClubSpecificOccupationCrudFeatureTest {
    private static final String OK_SSN = "111111-1012";

    @Resource
    private HuntingClubMemberInvitationFeature huntingClubInvitationFeature;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private HuntingClubMemberInvitationRepository invitationRepository;

    @Test
    public void testFindInvalidHunterNumbers() {
        withPerson(personWithArtificialSsn -> {
            personWithArtificialSsn.setSsn(artificialSsn());
            final HuntingClub club = model().newHuntingClub();
            model().newOccupation(club, personWithArtificialSsn, SEURAN_YHDYSHENKILO);

            final Person personWithRealSsn = model().newPerson();
            personWithRealSsn.setSsn(OK_SSN);

            onSavedAndAuthenticated(createUser(personWithArtificialSsn), () -> {
                final String hunterNumberNotFound = "11111111";
                final Set<String> queried = Sets.newHashSet(hunterNumberNotFound,
                        personWithArtificialSsn.getHunterNumber(), personWithRealSsn.getHunterNumber());
                final Set<String> expected = Sets.newHashSet(hunterNumberNotFound, personWithArtificialSsn.getHunterNumber());
                assertEquals(expected, huntingClubInvitationFeature.findInvalidHunterNumbers(club.getId(), queried));
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testInviteAuthorization_personNotClubContactPerson_noAccess() {
        final HuntingClub club = model().newHuntingClub();
        persistAndAuthenticateWithNewUser(true);
        huntingClubInvitationFeature.invite(club.getId(), null, emptySet());
    }

    @Test
    public void testInviteAuthorization_moderatorHasAccess_access() {
        final HuntingClub club = model().newHuntingClub();
        onSavedAndAuthenticated(createNewModerator(), () -> huntingClubInvitationFeature.invite(club.getId(), null, emptySet()));
    }

    @Test
    public void testInviteAuthorization_personIsClubContactPerson_access() {
        withPerson(person -> {
            final HuntingClub club = model().newHuntingClub();
            model().newOccupation(club, person, SEURAN_YHDYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> huntingClubInvitationFeature.invite(club.getId(), null, emptySet()));
        });
    }

    @Test
    public void testInvite_withGroup() {
        withPerson(person -> {
            final HuntingClub club = model().newHuntingClub();
            model().newOccupation(club, person, SEURAN_YHDYSHENKILO);
            final HuntingClubGroup group = model().newHuntingClubGroup(club);

            onSavedAndAuthenticated(createUser(person), () -> {
                huntingClubInvitationFeature.invite(club.getId(), group.getId(), emptySet());
            });
        });
    }

    @Test(expected = HuntingClubHasNoSuchGroupException.class)
    public void testInvite_withGroupNotOfSameClub() {
        withPerson(person -> withRhy(rhy -> {
            final HuntingClub club = model().newHuntingClub(rhy);
            model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);
            final HuntingClubGroup otherGroup = model().newHuntingClubGroup(model().newHuntingClub(rhy));

            onSavedAndAuthenticated(createUser(person), () -> {
                huntingClubInvitationFeature.invite(club.getId(), otherGroup.getId(), emptySet());
            });
        }));
    }

    @Test
    public void testInviteOk() {
        withPerson(person -> {
            final HuntingClub club = model().newHuntingClub();
            model().newOccupation(club, person, SEURAN_YHDYSHENKILO);

            final Person p1 = model().newPerson();

            onSavedAndAuthenticated(createUser(person), () -> {
                huntingClubInvitationFeature.invite(club.getId(), null, hunterNumbers(p1));
                assertInvites(club, p1);
            });
        });
    }

    @Test
    public void testNormalMemberCantSeeInvites() {
        testCanSeeInvitations(SEURAN_JASEN, false);
    }

    @Test
    public void testContactCanSeeInvites() {
        testCanSeeInvitations(SEURAN_YHDYSHENKILO, true);
    }

    private void testCanSeeInvitations(OccupationType clubMemberType, boolean canSee) {
        withPerson(person -> {
            final HuntingClub club = model().newHuntingClub();
            model().newOccupation(club, person, clubMemberType);

            model().newHuntingClubInvitation(club);

            onSavedAndAuthenticated(createUser(person), () -> {
                assertEquals(canSee ? 1 : 0, huntingClubInvitationFeature.listInvitations(club.getId()).size());
            });
        });
    }

    @Test
    public void testInviteAndAddToGroupOk() {
        withPerson(person -> {
            final HuntingClub club = model().newHuntingClub();
            model().newOccupation(club, person, SEURAN_YHDYSHENKILO);
            final HuntingClubGroup group = model().newHuntingClubGroup(club);

            final Person alreadyInvited = model().newPerson();
            model().newHuntingClubInvitation(alreadyInvited, club, SEURAN_JASEN);

            final Person notMember = model().newPerson();
            final Person alreadyClubMember = model().newHuntingClubMember(club, SEURAN_JASEN).getPerson();
            final Person alreadyGroupMember = model().newHuntingClubGroupMember(group, OccupationType.RYHMAN_JASEN).getPerson();

            final Occupation deletedClubMemberOccupation = model().newHuntingClubMember(club, SEURAN_JASEN);
            deletedClubMemberOccupation.softDelete();
            final Person deletedClubMember = deletedClubMemberOccupation.getPerson();

            final Occupation deletedGroupMemberOccupation = model().newHuntingClubGroupMember(group, OccupationType.RYHMAN_JASEN);
            deletedGroupMemberOccupation.softDelete();
            final Person deletedGroupMember = deletedGroupMemberOccupation.getPerson();

            onSavedAndAuthenticated(createUser(person), () -> {
                huntingClubInvitationFeature.invite(club.getId(), group.getId(),
                        hunterNumbers(alreadyInvited, notMember, alreadyClubMember, deletedClubMember, deletedGroupMember, alreadyGroupMember));

                assertInvites(club, alreadyInvited, notMember, deletedClubMember, deletedGroupMember, alreadyGroupMember);
                assertGroupMemberships(group, alreadyInvited, notMember, alreadyClubMember, deletedClubMember, deletedGroupMember, alreadyGroupMember);
            });
        });
    }

    private static Set<String> hunterNumbers(Person... p) {
        return Stream.of(p).map(Person::getHunterNumber).collect(toSet());
    }

    private void assertInvites(HuntingClub club, Person... expected) {
        runInTransaction(() -> {
            final List<HuntingClubMemberInvitation> invitations = invitationRepository.getByHuntingClub(club);
            assertEquals(expected.length, invitations.size());

            assertEquals(F.getUniqueIds(expected),
                    F.getUniqueIds(invitations.stream()
                            .map(HuntingClubMemberInvitation::getPerson)
                            .collect(toSet()))
            );
        });
    }

    private void assertGroupMemberships(HuntingClubGroup group, Person... expected) {
        runInTransaction(() -> {
            final List<Occupation> members = occupationRepository.findAll(JpaSpecs.and(
                    JpaSpecs.equal(Occupation_.organisation, group),
                    JpaSpecs.notSoftDeleted()
            ));
            assertEquals(expected.length, members.size());

            assertEquals(F.getUniqueIds(expected),
                    F.getUniqueIds(members.stream()
                            .map(Occupation::getPerson)
                            .collect(toSet())));
        });
    }
}
