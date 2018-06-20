package fi.riista.feature.huntingclub.members.club;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.members.AbstractClubSpecificOccupationCrudFeatureTest;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Function;

import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.feature.organization.person.ContactInfoShare.ALL_MEMBERS;
import static fi.riista.feature.organization.person.ContactInfoShare.ONLY_OFFICIALS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class HuntingClubMemberCrudFeatureTest extends AbstractClubSpecificOccupationCrudFeatureTest {

    @Resource
    private HuntingClubMemberCrudFeature crudFeature;

    @Resource
    private OccupationRepository occupationRepository;

    @Test
    public void testContactInfoShare_clubMember() {
        final Function<Long, List<OccupationDTO>> getMembers = orgId -> crudFeature.listMembers(orgId);
        doTestContactInfoShare(getMembers, model().newHuntingClub(), SEURAN_JASEN, false, null, false);
        doTestContactInfoShare(getMembers, model().newHuntingClub(), SEURAN_JASEN, false, ONLY_OFFICIALS, false);
        doTestContactInfoShare(getMembers, model().newHuntingClub(), SEURAN_JASEN, true, ALL_MEMBERS, false);
    }

    @Test
    public void testContactInfoShare_clubContact() {
        final Function<Long, List<OccupationDTO>> getMembers = orgId -> crudFeature.listMembers(orgId);
        doTestContactInfoShare(getMembers, model().newHuntingClub(), SEURAN_YHDYSHENKILO, false, null, true);
        doTestContactInfoShare(getMembers, model().newHuntingClub(), SEURAN_YHDYSHENKILO, true, ONLY_OFFICIALS, true);
        doTestContactInfoShare(getMembers, model().newHuntingClub(), SEURAN_YHDYSHENKILO, true, ALL_MEMBERS, true);
    }

    @Test
    public void testDeletingClubMemberDeletesGroupMemberships() {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);

        final Occupation member1 = model().newHuntingClubMember(club, SEURAN_YHDYSHENKILO);

        final Occupation member2 = model().newHuntingClubMember(club, SEURAN_JASEN);
        final Occupation groupMember2_1 = model().newHuntingClubGroupMember(member2.getPerson(), group);

        onSavedAndAuthenticated(createUser(member1.getPerson()), () -> {
            crudFeature.delete(member2.getId());

            runInTransaction(() -> {
                assertEquals(3, occupationRepository.count());
                assertIsDeleted(member1.getId(), false);
                assertIsDeleted(member2.getId(), true);
                assertIsDeleted(groupMember2_1.getId(), true);
            });
        });
    }

    private void assertIsDeleted(long id, boolean isDeleted) {
        final Occupation occ = occupationRepository.getOne(id);
        assertEquals(isDeleted, occ.isDeleted());
        assertEquals(isDeleted ? DateUtil.today() : null,
                occ.getEndDate());
    }

    @Test
    public void testUpdateOccupationTypeDoesNotDeleteGroupMemberships() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation clubContact = model().newHuntingClubMember(club, SEURAN_YHDYSHENKILO);
        final Occupation clubMembership = model().newHuntingClubMember(club, SEURAN_JASEN);
        final Person member = clubMembership.getPerson();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        final Occupation groupMembership = model().newHuntingClubGroupMember(member, group);

        onSavedAndAuthenticated(createUser(clubContact.getPerson()), () -> {
            final OccupationDTO clubMembershipUpdated = crudFeature.updateOccupationType(
                    clubMembership.getId(), SEURAN_YHDYSHENKILO);

            runInTransaction(() -> {
                assertEquals(
                        F.getUniqueIds(clubMembershipUpdated, groupMembership),
                        F.getUniqueIds(occupationRepository.findActiveByPerson(member)));
            });
        });
    }

    @Test
    public void testUpdateOccupationTypePreservesContactInfoShare() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation clubContact = model().newHuntingClubMember(club, SEURAN_YHDYSHENKILO);
        final Occupation clubMembership = model().newHuntingClubMember(club, SEURAN_JASEN);
        clubMembership.setContactInfoShare(ALL_MEMBERS);

        onSavedAndAuthenticated(createUser(clubContact.getPerson()), () -> {
            final OccupationDTO clubMembershipUpdated = crudFeature.updateOccupationType(
                    clubMembership.getId(), SEURAN_YHDYSHENKILO);

            assertEquals(clubMembership.getContactInfoShare(), clubMembershipUpdated.getContactInfoShare());
        });
    }

    @Test
    public void testResignedMembersAreNotListed() {
        final HuntingClub club = model().newHuntingClub();

        final Occupation member1 = model().newHuntingClubMember(club, SEURAN_YHDYSHENKILO);

        final Occupation member2 = model().newHuntingClubMember(club, SEURAN_YHDYSHENKILO);
        member2.setEndDate(DateUtil.today().minusDays(1));

        final Occupation member3 = model().newHuntingClubMember(club, SEURAN_YHDYSHENKILO);
        member3.softDelete();

        onSavedAndAuthenticated(createUser(member1.getPerson()), () -> {
            List<OccupationDTO> members = crudFeature.listMembers(club.getId());
            assertEquals(F.getUniqueIds(member1), F.getUniqueIds(members));
        });
    }

    @Test
    public void testUpdateOccupationTypeUpdatesContactPersonCallOrder() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation firstContactOccupation = model().newHuntingClubMember(club, SEURAN_YHDYSHENKILO);
        final Occupation secondContactOccupation = model().newHuntingClubMember(club, SEURAN_YHDYSHENKILO);
        firstContactOccupation.setCallOrder(0);
        secondContactOccupation.setCallOrder(null);

        onSavedAndAuthenticated(createUser(firstContactOccupation.getPerson()), () -> {
            final OccupationDTO clubMembershipUpdated = crudFeature.updateOccupationType(
                    firstContactOccupation.getId(), SEURAN_JASEN);

            assertNull(clubMembershipUpdated.getCallOrder());

            runInTransaction(() -> {
                final List<Occupation> activeOccupations = occupationRepository
                        .findActiveByOrganisationAndOccupationType(club, SEURAN_YHDYSHENKILO);
                assertEquals(1, activeOccupations.size());

                for (Occupation activeOccupation : activeOccupations) {
                    assertEquals(secondContactOccupation.getPerson(), activeOccupation.getPerson());
                    assertNotNull(activeOccupation.getCallOrder());
                    assertEquals(0, (int) activeOccupation.getCallOrder());
                }
            });
        });
    }

    @Test
    public void testDeleteContactPersonUpdatesCallOrder() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation firstContactOccupation = model().newHuntingClubMember(club, SEURAN_YHDYSHENKILO);
        final Occupation secondContactOccupation = model().newHuntingClubMember(club, SEURAN_YHDYSHENKILO);
        firstContactOccupation.setCallOrder(0);
        secondContactOccupation.setCallOrder(null);

        onSavedAndAuthenticated(createUser(firstContactOccupation.getPerson()), () -> {
            crudFeature.delete(firstContactOccupation.getId());

            runInTransaction(() -> {
                final List<Occupation> activeOccupations = occupationRepository.findActiveByOrganisation(club);
                assertEquals(1, activeOccupations.size());

                for (Occupation activeOccupation : activeOccupations) {
                    assertEquals(secondContactOccupation.getPerson(), activeOccupation.getPerson());
                    assertEquals(0, (int) activeOccupation.getCallOrder());
                }
            });
        });
    }
}
