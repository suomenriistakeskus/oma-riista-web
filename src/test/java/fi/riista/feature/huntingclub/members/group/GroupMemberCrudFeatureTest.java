package fi.riista.feature.huntingclub.members.group;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.members.AbstractClubSpecificOccupationCrudFeatureTest;
import fi.riista.feature.mail.queue.MailMessageRecipient;
import fi.riista.feature.mail.queue.MailMessageRecipientRepository;
import fi.riista.feature.mail.queue.MailMessageRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.matchers.OccupationMatchers;
import fi.riista.util.F;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.person.ContactInfoShare.ALL_MEMBERS;
import static fi.riista.feature.organization.person.ContactInfoShare.ONLY_OFFICIALS;
import static fi.riista.feature.organization.person.ContactInfoShare.SAME_PERMIT_LEVEL;

import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

public class GroupMemberCrudFeatureTest extends AbstractClubSpecificOccupationCrudFeatureTest
        implements HuntingGroupFixtureMixin {

    @Resource
    private GroupMemberCrudFeature crudFeature;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private MailMessageRecipientRepository recipientRepository;

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
    public void testContactInfoShareIsNullByDefault() {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        withPerson(person -> {
            final Occupation clubMembership = model().newOccupation(club, person, SEURAN_JASEN);
            clubMembership.setContactInfoShare(ContactInfoShare.ALL_MEMBERS);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                OccupationDTO dto = crudFeature.create(createDto(group, person, RYHMAN_JASEN));
                assertEquals(null, dto.getContactInfoShare());
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

    @Test
    public void testLeaderCreateMember_personAlreadyLeader() {
        withMooseHuntingGroupFixture(fixture -> onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
            final OccupationDTO createdLeader = doCreateGroupMember(fixture, RYHMAN_METSASTYKSENJOHTAJA);

            final OccupationDTO duplicateOccupation = new OccupationDTO();
            duplicateOccupation.setPersonId(createdLeader.getPersonId());
            duplicateOccupation.setOccupationType(RYHMAN_JASEN);
            duplicateOccupation.setOrganisationId(createdLeader.getOrganisationId());

            assertThrows(IllegalArgumentException.class, () -> crudFeature.create(duplicateOccupation));
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

    private OccupationDTO doCreateGroupMember(final HuntingGroupFixture fixture, final OccupationType occType) {
        final OccupationDTO dto = new OccupationDTO();
        dto.setPersonId(fixture.clubMember.getId());
        dto.setOrganisationId(fixture.group.getId());
        dto.setOccupationType(occType);
        return crudFeature.create(dto);
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

    // Hunting leader contact info visibility
    @Test
    public void testSavingVisibilityModifiesAllOccupationsInThePermit() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermit permit = model().newMooselikePermit(rhy);
        final HarvestPermitSpeciesAmount spa = model().newHarvestPermitSpeciesAmount(permit, model().newGameSpeciesMoose());

        withHuntingGroupFixture(spa, group1fixture -> {
            withHuntingGroupFixture(spa, group2fixture -> {
                model().newOccupation(group2fixture.club, group1fixture.groupLeader, OccupationType.SEURAN_JASEN);
                model().newOccupation(group2fixture.group, group1fixture.groupLeader, RYHMAN_METSASTYKSENJOHTAJA);

                onSavedAndAuthenticated(createUser(group1fixture.groupLeader), () -> {
                    ContactInfoShareAndVisibilityUpdateDTO dto = createDto(permit);
                    crudFeature.updateContactInfoSharingAndVisibility(Collections.singletonList(dto));
                });

                runInTransaction(() -> {
                    final List<Occupation> activeOccupations = occupationRepository.findActiveByPerson(group1fixture.groupLeader).stream()
                            .filter(occ -> occ.getOccupationType() == RYHMAN_METSASTYKSENJOHTAJA)
                            .collect(Collectors.toList());

                    assertThat(activeOccupations, hasSize(2));
                    activeOccupations.forEach(occ -> {
                        assertThat(occ.getContactInfoShare(), equalTo(SAME_PERMIT_LEVEL));
                        assertThat(occ.isNameVisibility(), equalTo(true));
                        assertThat(occ.isPhoneNumberVisibility(), equalTo(true));
                        assertThat(occ.isEmailVisibility(), equalTo(true));
                    });
                });
            });
        });

    }

    @Test
    public void testSavingVisibilityModifiesOnlyOccupationsInThePermit() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies moose = model().newGameSpeciesMoose();

        final HarvestPermit permit1 = model().newMooselikePermit(rhy);
        final HarvestPermitSpeciesAmount spa1 = model().newHarvestPermitSpeciesAmount(permit1, moose);
        final HarvestPermit permit2 = model().newMooselikePermit(rhy);
        final HarvestPermitSpeciesAmount spa2 = model().newHarvestPermitSpeciesAmount(permit2, moose);

        withHuntingGroupFixture(spa1, group1fixture -> {
            withHuntingGroupFixture(spa2, group2fixture -> {
                model().newOccupation(group2fixture.club, group1fixture.groupLeader, OccupationType.SEURAN_JASEN);
                model().newOccupation(group2fixture.group, group1fixture.groupLeader, RYHMAN_METSASTYKSENJOHTAJA);

                onSavedAndAuthenticated(createUser(group1fixture.groupLeader), () -> {
                    ContactInfoShareAndVisibilityUpdateDTO dto = createDto(permit1);
                    crudFeature.updateContactInfoSharingAndVisibility(Collections.singletonList(dto));
                });

                runInTransaction(() -> {
                    final List<Occupation> activeOccupations = occupationRepository.findActiveByPerson(group1fixture.groupLeader).stream()
                            .filter(occ -> occ.getOccupationType() == RYHMAN_METSASTYKSENJOHTAJA)
                            .collect(Collectors.toList());

                    assertThat(activeOccupations, hasSize(2));
                    final Map<Organisation, Occupation> occupationsByGroup = F.index(activeOccupations, Occupation::getOrganisation);
                    Occupation group1Occupation = occupationsByGroup.get(group1fixture.group);
                    assertThat(group1Occupation.getContactInfoShare(), equalTo(SAME_PERMIT_LEVEL));
                    assertThat(group1Occupation.isNameVisibility(), equalTo(true));
                    assertThat(group1Occupation.isPhoneNumberVisibility(), equalTo(true));
                    assertThat(group1Occupation.isEmailVisibility(), equalTo(true));

                    final Occupation group2Occupation = occupationsByGroup.get(group2fixture.group);
                    assertThat(group2Occupation.getContactInfoShare(), Matchers.is(nullValue()),
                            "Group 2 occupation contact info share should still be null");

                });
            });
        });
    }

    @Test
    public void testAddingLeaderOccupationCopiesContactShareInfoFromOtherOccupation() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermit permit = model().newMooselikePermit(rhy);
        final HarvestPermitSpeciesAmount spa = model().newHarvestPermitSpeciesAmount(permit, model().newGameSpeciesMoose());
        withHuntingGroupFixture(spa, group1fixture -> {

            group1fixture.groupLeaderOccupation.setContactInfoShare(SAME_PERMIT_LEVEL);
            group1fixture.groupLeaderOccupation.setNameVisibility(true);
            group1fixture.groupLeaderOccupation.setPhoneNumberVisibility(true);
            group1fixture.groupLeaderOccupation.setEmailVisibility(true);
            persistInNewTransaction();

            withHuntingGroupFixture(spa, group2fixture -> {
                model().newOccupation(group2fixture.club, group1fixture.groupLeader, OccupationType.SEURAN_JASEN);
                final Occupation newOccupation =
                        model().newOccupation(group2fixture.group, group1fixture.groupLeader, RYHMAN_JASEN);

                onSavedAndAuthenticated(createUser(group2fixture.clubContact), () -> {
                    final OccupationDTO dto = crudFeature.updateOccupationType(newOccupation.getId(), RYHMAN_METSASTYKSENJOHTAJA);
                    assertThat(dto.getContactInfoShare(), equalTo(SAME_PERMIT_LEVEL));
                });

                final List<Occupation> activeOccupations = occupationRepository.findActiveByPerson(group1fixture.groupLeader).stream()
                        .filter(occ -> occ.getOccupationType() == RYHMAN_METSASTYKSENJOHTAJA)
                        .collect(Collectors.toList());

                assertThat(activeOccupations, hasSize(2));
                activeOccupations.forEach(occ -> {
                    assertThat(occ.getContactInfoShare(), equalTo(SAME_PERMIT_LEVEL));
                    assertThat(occ.isNameVisibility(), equalTo(true));
                    assertThat(occ.isPhoneNumberVisibility(), equalTo(true));
                    assertThat(occ.isEmailVisibility(), equalTo(true));
                });
            });
        });
    }

    // Hunting leader emails
    @Test
    public void testEmailSentWhenAddingUserAsHuntingLeader() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermit permit = model().newMooselikePermit(rhy);
        final HarvestPermitSpeciesAmount spa = model().newHarvestPermitSpeciesAmount(permit, model().newGameSpeciesMoose());

        withHuntingGroupFixture(spa, fixture -> {
            Person person = model().newPerson();
            model().newUser(person);
            model().newOccupation(fixture.club, person, OccupationType.SEURAN_JASEN);
            Occupation groupMemberOccupation = model().newOccupation(fixture.group, person, RYHMAN_JASEN);

            onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
                crudFeature.updateOccupationType(groupMemberOccupation.getId(), RYHMAN_METSASTYKSENJOHTAJA);
            });

            runInTransaction(() -> {
                List<MailMessageRecipient> recipients = recipientRepository.findAll();

                assertThat(recipients, hasSize(1));
                assertThat(recipients.get(0).getEmail(), equalTo(person.getEmail()));
            });
        });
    }

    @Test
    public void testEmailSentWhenAddingNotActiveUserAsHuntingLeader() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermit permit = model().newMooselikePermit(rhy);
        final HarvestPermitSpeciesAmount spa = model().newHarvestPermitSpeciesAmount(permit, model().newGameSpeciesMoose());

        withHuntingGroupFixture(spa, fixture -> {
            Person person = model().newPerson();
            SystemUser user = model().newUser(person);
            user.setActive(false);

            model().newOccupation(fixture.club, person, OccupationType.SEURAN_JASEN);
            Occupation groupMemberOccupation = model().newOccupation(fixture.group, person, RYHMAN_JASEN);

            onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
                crudFeature.updateOccupationType(groupMemberOccupation.getId(), RYHMAN_METSASTYKSENJOHTAJA);
            });

            runInTransaction(() -> {
                List<MailMessageRecipient> recipients = recipientRepository.findAll();

                assertThat(recipients, Is.is(empty()));
            });
        });
    }
    @Test
    public void testEmailNotSentWhenAddingNonUserAsHuntingLeader() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermit permit = model().newMooselikePermit(rhy);
        final HarvestPermitSpeciesAmount spa = model().newHarvestPermitSpeciesAmount(permit, model().newGameSpeciesMoose());

        withHuntingGroupFixture(spa, fixture -> {
            Person person = model().newPerson(); // No user attached
            model().newOccupation(fixture.club, person, OccupationType.SEURAN_JASEN);
            Occupation groupMemberOccupation = model().newOccupation(fixture.group, person, RYHMAN_JASEN);

            onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
                crudFeature.updateOccupationType(groupMemberOccupation.getId(), RYHMAN_METSASTYKSENJOHTAJA);
            });

            runInTransaction(() -> {
                List<MailMessageRecipient> recipients = recipientRepository.findAll();

                assertThat(recipients, Is.is(empty()));
            });
        });
    }

    private ContactInfoShareAndVisibilityUpdateDTO createDto(final HarvestPermit permit) {
        final ContactInfoShareAndVisibilityUpdateDTO dto = new ContactInfoShareAndVisibilityUpdateDTO();
        dto.setPermitId(permit.getId());
        dto.setShare(ContactInfoShare.SAME_PERMIT_LEVEL);
        dto.setNameVisibility(true);
        dto.setPhoneNumberVisibility(true);
        dto.setEmailVisibility(true);
        return dto;
    }

}
