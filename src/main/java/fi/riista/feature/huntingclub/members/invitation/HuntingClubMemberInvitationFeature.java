package fi.riista.feature.huntingclub.members.invitation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.Occupation_;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.person.Person_;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.jpa.JpaSpecs;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static fi.riista.util.DateUtil.today;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;

@Component
public class HuntingClubMemberInvitationFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private HuntingClubMemberInvitationRepository invitationRepository;

    @Resource
    private HuntingClubGroupRepository groupRepository;

    @Transactional(readOnly = true)
    public Set<String> findInvalidHunterNumbers(final Long clubId, final Set<String> hunterNumbers) {
        requireEntityService.requireHuntingClub(clubId, EntityPermission.UPDATE);

        final Set<String> foundHunterNumbers = findPersonsByHunterNumber(hunterNumbers)
                .stream()
                .filter(p -> !p.isArtificialPerson() && !p.isDeceased())
                .map(Person::getHunterNumber)
                .collect(toSet());
        return Sets.difference(hunterNumbers, foundHunterNumbers);
    }

    @Transactional
    public void invite(final Long clubId, final Long groupId, final Set<String> hunterNumbers) {
        final HuntingClub club = requireEntityService.requireHuntingClub(clubId, EntityPermission.UPDATE);

        final HuntingClubGroup group = findGroupAndAssertIsClubsGroup(clubId, groupId);

        final Set<Person> personsByHunterNumbers = new HashSet<>(findPersonsByHunterNumber(hunterNumbers));

        final Set<Person> personsToBeInvited = findPersonsNotMembersAndNotHavingInvitations(club,
                personsByHunterNumbers);
        if (!personsToBeInvited.isEmpty()) {
            invitationRepository.save(personsToBeInvited.stream()
                    .map(person -> new HuntingClubMemberInvitation(person, club, OccupationType.SEURAN_JASEN))
                    .collect(toSet()));
        }
        if (group != null) {
            final Set<Person> personsToAddToGroup = findPersonsNotHavingOccupation(group, personsByHunterNumbers);
            if (!personsToAddToGroup.isEmpty()) {
                occupationRepository.save(personsToAddToGroup.stream()
                        .map(person -> new Occupation(person, group, OccupationType.RYHMAN_JASEN))
                        .collect(toSet()));
            }
        }
    }

    private Set<Person> findPersonsNotMembersAndNotHavingInvitations(final HuntingClub club,
                                                                     final Set<Person> persons) {
        final Set<Person> havingInvitation = invitationRepository.findAll(JpaSpecs.and(
                JpaSpecs.equal(HuntingClubMemberInvitation_.huntingClub, club),
                JpaSpecs.inCollection(HuntingClubMemberInvitation_.person, persons),
                JpaSpecs.fetch(HuntingClubMemberInvitation_.person)
        )).stream().map(HuntingClubMemberInvitation::getPerson).collect(toSet());
        final Set<Person> notMembers = findPersonsNotHavingOccupation(club, persons);
        return Sets.difference(notMembers, havingInvitation);
    }

    private Set<Person> findPersonsNotHavingOccupation(final Organisation org, final Set<Person> persons) {
        final EnumSet<OccupationType> occTypes = OccupationType.getApplicableTypes(org.getOrganisationType());
        final Set<Person> havingOccupation = occupationRepository.findAll(JpaSpecs.and(
                JpaSpecs.equal(Occupation_.organisation, org),
                JpaSpecs.inCollection(Occupation_.occupationType, occTypes),
                JpaSpecs.inCollection(Occupation_.person, persons),
                JpaSpecs.notSoftDeleted(),
                JpaSpecs.withinInterval(Occupation_.beginDate, Occupation_.endDate, today()),
                JpaSpecs.fetch(Occupation_.person)
        )).stream()
                .map(Occupation::getPerson)
                .collect(toSet());
        return Sets.difference(persons, havingOccupation);
    }

    private HuntingClubGroup findGroupAndAssertIsClubsGroup(final Long clubId, final Long groupId) {
        if (groupId == null) {
            return null;
        }
        final HuntingClubGroup group = groupRepository.getOne(groupId);
        if (!Objects.equals(clubId, group.getParentOrganisation().getId())) {
            throw new HuntingClubHasNoSuchGroupException(clubId, groupId);
        }
        return group;
    }

    @Transactional
    public void reSendInvitation(Long id) {
        final HuntingClubMemberInvitation invitation = getInvitationAndAssertUpdatePermission(id);
        invitation.reSend();
    }

    @Transactional
    public void deleteInvitation(Long id) {
        final HuntingClubMemberInvitation invitation = getInvitationAndAssertUpdatePermission(id);
        invitationRepository.delete(invitation);
    }

    private HuntingClubMemberInvitation getInvitationAndAssertUpdatePermission(Long id) {
        final HuntingClubMemberInvitation invitation = invitationRepository.getOne(id);
        final HuntingClub club = invitation.getHuntingClub();
        activeUserService.assertHasPermission(club, EntityPermission.UPDATE);
        return invitation;
    }

    @Transactional(readOnly = true)
    public List<HuntingClubMemberInvitationDTO> listInvitations(final Long clubId) {
        final HuntingClub club = requireEntityService.requireHuntingClub(clubId, EntityPermission.READ);

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Person person = activeUser.getPerson();
        final boolean showInvitations =
                person == null ? activeUser.isModeratorOrAdmin() : userAuthorizationHelper.isClubContact(club, person);

        if (!showInvitations) {
            return emptyList();
        }
        final JpaSort sort = new JpaSort(Sort.Direction.ASC,
                JpaSort.path(HuntingClubMemberInvitation_.person).dot(Person_.lastName),
                JpaSort.path(HuntingClubMemberInvitation_.person).dot(Person_.byName));
        return HuntingClubMemberInvitationDTO.create(
                invitationRepository.findAll(JpaSpecs.equal(HuntingClubMemberInvitation_.huntingClub, club), sort),
                true);
    }

    @Transactional(readOnly = true)
    public List<HuntingClubMemberInvitationDTO> listMyInvitations(Long personId) {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        Person person = activeUser.getPerson();

        if (person == null) {
            if (personId != null && activeUser.isModeratorOrAdmin()) {
                person = personRepository.getOne(personId);
            } else {
                return emptyList();
            }
        }

        return HuntingClubMemberInvitationDTO.create(invitationRepository.findAll(JpaSpecs.and(
                JpaSpecs.equal(HuntingClubMemberInvitation_.person, person),
                JpaSpecs.isNull(HuntingClubMemberInvitation_.userRejectedTime)
        )));
    }

    @Transactional
    public OccupationDTO acceptInvitation(Long invitationId) {
        HuntingClubMemberInvitation invitation = getInvitationAndAssertCurrentPerson(invitationId);
        OccupationDTO dto = new OccupationDTO();
        dto.setPersonId(invitation.getPerson().getId());
        dto.setOrganisationId(invitation.getHuntingClub().getId());
        dto.setOccupationType(invitation.getOccupationType());

        invitationRepository.delete(invitation);

        return dto;
    }

    @Transactional
    public void rejectInvitation(Long invitationId) {
        HuntingClubMemberInvitation invitation = getInvitationAndAssertCurrentPerson(invitationId);
        invitation.setUserRejectedTime(DateUtil.now());
    }

    private HuntingClubMemberInvitation getInvitationAndAssertCurrentPerson(Long invitationId) {
        final HuntingClubMemberInvitation invitation = invitationRepository.getOne(invitationId);
        final Person person = activeUserService.requireActiveUser().getPerson();
        Preconditions.checkState(Objects.equals(person, invitation.getPerson()),
                "Current person is not the invited person, invitationId:" + invitationId);
        return invitation;
    }

    private List<Person> findPersonsByHunterNumber(Set<String> hunterNumbers) {
        if (hunterNumbers.isEmpty()) {
            return emptyList();
        }

        return personRepository.findFinnishPersonsByHunterNumber(hunterNumbers);
    }
}
