package fi.riista.feature.huntingclub.members.group;

import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.members.AbstractClubSpecificOccupationCrudFeatureTest;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.matchers.OccupationMatchers;
import fi.riista.util.F;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Function;

import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.person.ContactInfoShare.ALL_MEMBERS;
import static fi.riista.feature.organization.person.ContactInfoShare.ONLY_OFFICIALS;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class GroupMemberCrudFeatureTest extends AbstractClubSpecificOccupationCrudFeatureTest
        implements HuntingGroupFixtureMixin {

    @Resource
    private GroupMemberCrudFeature crudFeature;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Before
    public void disablePermitLockByDate() {
        harvestPermitLockedByDateService.disableLockingForTests();
    }

    @After
    public void enablePermitLockByDate() {
        harvestPermitLockedByDateService.normalLocking();
    }

    @Test
    public void testContactInfoShare_groupMember() {
        final Function<Long, List<OccupationDTO>> getMembers = orgId -> crudFeature.listMembers(orgId);
        doTestContactInfoShare(getMembers, model().newHuntingClubGroup(), RYHMAN_JASEN, false, null, false);
        doTestContactInfoShare(getMembers, model().newHuntingClubGroup(), RYHMAN_JASEN, false, ONLY_OFFICIALS, false);
        doTestContactInfoShare(getMembers, model().newHuntingClubGroup(), RYHMAN_JASEN, true, ALL_MEMBERS, false);
    }

    @Test
    public void testContactInfoShare_groupHuntingLeader() {
        final Function<Long, List<OccupationDTO>> getMembers = orgId -> crudFeature.listMembers(orgId);
        doTestContactInfoShare(getMembers, model().newHuntingClubGroup(), RYHMAN_METSASTYKSENJOHTAJA, false, null, true);
        doTestContactInfoShare(getMembers, model().newHuntingClubGroup(), RYHMAN_METSASTYKSENJOHTAJA, true, ONLY_OFFICIALS, true);
        doTestContactInfoShare(getMembers, model().newHuntingClubGroup(), RYHMAN_METSASTYKSENJOHTAJA, true, ALL_MEMBERS, true);
    }

    @Test
    public void testLeaderRoleHasCallOrderSet() {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        final Person p1 = model().newPerson();
        final Person p2 = model().newPerson();
        final Person p3 = model().newPerson();
        final Person p4 = model().newPerson();
        model().newOccupation(club, p1, SEURAN_JASEN);
        model().newOccupation(club, p2, SEURAN_JASEN);
        model().newOccupation(club, p3, SEURAN_JASEN);
        model().newOccupation(club, p4, SEURAN_JASEN);

        onSavedAndAuthenticated(createNewModerator(), () -> {

            final OccupationDTO dto1 = crudFeature.create(createDto(group, p1, RYHMAN_JASEN));
            final OccupationDTO dto2 = crudFeature.create(createDto(group, p2, RYHMAN_METSASTYKSENJOHTAJA));
            final OccupationDTO dto3 = crudFeature.create(createDto(group, p3, RYHMAN_METSASTYKSENJOHTAJA));
            final OccupationDTO dto4 = crudFeature.create(createDto(group, p4, RYHMAN_METSASTYKSENJOHTAJA));

            runInTransaction(() -> assertThat(occupationRepository.findByOrganisation(group), contains(
                    OccupationMatchers.hasCallOrder(equalTo(null)),
                    OccupationMatchers.hasCallOrder(equalTo(0)),
                    OccupationMatchers.hasCallOrder(equalTo(1)),
                    OccupationMatchers.hasCallOrder(equalTo(2))
            )));


            // change primary hunting leader to member, there still should be primary hunting leader
            final OccupationDTO dto2_2 = crudFeature.updateOccupationType(dto2.getId(), RYHMAN_JASEN);
            runInTransaction(() -> assertThat(occupationRepository.findNotDeletedByOrganisation(group), contains(
                    OccupationMatchers.hasCallOrder(equalTo(null)),
                    OccupationMatchers.hasCallOrder(equalTo(null)),
                    OccupationMatchers.hasCallOrder(equalTo(0)),
                    OccupationMatchers.hasCallOrder(equalTo(1))
            )));
        });
    }

    @Test
    public void testContactInfoShareIsCopiedFromClubMembership() {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        withPerson(person -> {
            final Occupation clubMembership = model().newOccupation(club, person, SEURAN_JASEN);
            clubMembership.setContactInfoShare(ContactInfoShare.ALL_MEMBERS);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                OccupationDTO dto = crudFeature.create(createDto(group, person, RYHMAN_JASEN));
                assertEquals(ContactInfoShare.ALL_MEMBERS, dto.getContactInfoShare());
            });
        });
    }

    @Test
    public void testContactInfoShareIsNotCopiedWhenMemberIsInvited() {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        withPerson(person -> {
            model().newHuntingClubInvitation(person, club, SEURAN_JASEN);
            onSavedAndAuthenticated(createNewModerator(), () -> {
                OccupationDTO dto = crudFeature.create(createDto(group, person, RYHMAN_JASEN));
                assertNull(dto.getContactInfoShare());
            });
        });
    }

    private static OccupationDTO createDto(final HuntingClubGroup group, final Person person, final OccupationType type) {
        final OccupationDTO dto = new OccupationDTO();
        dto.setOrganisationId(group.getId());
        dto.setPersonId(person.getId());
        dto.setOccupationType(type);
        return dto;
    }

    // Create member

    @Test
    public void testContactCreateMember() {
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
            doCreateGroupMember(fixture, OccupationType.RYHMAN_JASEN);
        }));
    }

    @Test
    public void testLeaderCreateMember() {
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(createUser(fixture.groupLeader), () -> {
            doCreateGroupMember(fixture, OccupationType.RYHMAN_JASEN);
        }));
    }

    // Create leader

    @Test
    public void testContactCreateLeader() {
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
            doCreateGroupMember(fixture, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        }));
    }

    @Test(expected = IllegalStateException.class)
    public void testContactCreateLeaderUnder18() {
        withMooseHuntingGroupFixture(fixture -> {
            fixture.clubMember.setSsn("010117A313X");
            onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
                doCreateGroupMember(fixture, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testLeaderCreateLeader() {
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(createUser(fixture.groupLeader), () -> {
            doCreateGroupMember(fixture, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        }));
    }

    private void doCreateGroupMember(final HuntingGroupFixture fixture, final OccupationType occType) {
        final OccupationDTO dto = new OccupationDTO();
        dto.setPersonId(fixture.clubMember.getId());
        dto.setOrganisationId(fixture.group.getId());
        dto.setOccupationType(occType);
        crudFeature.create(dto);
    }

    // Change member to leader

    @Test
    public void testContactChangeMemberToLeader() {
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
            crudFeature.updateOccupationType(fixture.groupMemberOccupation.getId(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        }));
    }

    @Test(expected = IllegalStateException.class)
    public void testContactChangeMemberToLeaderUnder18() {
        withMooseHuntingGroupFixture(fixture -> {
            fixture.groupMemberOccupation.getPerson().setSsn("010117A313X");
            onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
                crudFeature.updateOccupationType(fixture.groupMemberOccupation.getId(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testLeaderChangeMemberToLeader() {
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(createUser(fixture.groupLeader), () -> {
            crudFeature.updateOccupationType(fixture.groupMemberOccupation.getId(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        }));
    }

    // Change leader to member

    @Test
    public void testContactChangeLeaderToMember() {
        withMooseHuntingGroupFixture(fixture -> {
            final Occupation newOcc = model().newHuntingClubGroupMember(fixture.group, RYHMAN_METSASTYKSENJOHTAJA);
            onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
                crudFeature.updateOccupationType(newOcc.getId(), OccupationType.RYHMAN_JASEN);
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testLeaderChangeLeaderToMember() {
        withMooseHuntingGroupFixture(fixture -> {
            final Occupation newOcc = model().newHuntingClubGroupMember(fixture.group, RYHMAN_METSASTYKSENJOHTAJA);
            onSavedAndAuthenticated(createUser(fixture.groupLeader), () -> {
                crudFeature.updateOccupationType(newOcc.getId(), OccupationType.RYHMAN_JASEN);
            });
        });
    }

    // Delete member

    @Test
    public void testContactDeleteMember() {
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
            crudFeature.delete(fixture.groupMemberOccupation.getId());
        }));
    }

    @Test
    public void testLeaderDeleteMember() {
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(createUser(fixture.groupLeader), () -> {
            crudFeature.delete(fixture.groupMemberOccupation.getId());
        }));
    }

    // Delete leader

    @Test
    public void testContactDeleteLeader() {
        withMooseHuntingGroupFixture(fixture -> {
            final Occupation newOcc = model().newHuntingClubGroupMember(fixture.group, RYHMAN_METSASTYKSENJOHTAJA);
            onSavedAndAuthenticated(createUser(fixture.clubContact), () -> crudFeature.delete(newOcc.getId()));
        });

    }

    @Test(expected = AccessDeniedException.class)
    public void testLeaderDeleteLeader() {
        withMooseHuntingGroupFixture(fixture -> {
            final Occupation newOcc = model().newHuntingClubGroupMember(fixture.group, RYHMAN_METSASTYKSENJOHTAJA);
            onSavedAndAuthenticated(createUser(fixture.groupLeader), () -> crudFeature.delete(newOcc.getId()));
        });
    }

    // Update call order

    @Test
    public void testContactChangesLeaderCallOrder() {
        withMooseHuntingGroupFixture(fixture -> {
            final Occupation newOcc = model().newHuntingClubGroupMember(fixture.group, RYHMAN_METSASTYKSENJOHTAJA);
            onSavedAndAuthenticated(createUser(fixture.clubContact), () ->
                    crudFeature.updateContactOrder(fixture.group.getId(), F.getNonNullIds(newOcc, fixture.groupLeader)));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testLeaderChangesLeaderCallOrder() {
        withMooseHuntingGroupFixture(fixture -> {
            final Occupation newOcc = model().newHuntingClubGroupMember(fixture.group, RYHMAN_METSASTYKSENJOHTAJA);
            onSavedAndAuthenticated(createUser(fixture.groupLeader), () ->
                    crudFeature.updateContactOrder(fixture.group.getId(), F.getNonNullIds(newOcc, fixture.groupLeader)));
        });
    }
}
