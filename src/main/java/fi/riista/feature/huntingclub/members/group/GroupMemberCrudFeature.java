package fi.riista.feature.huntingclub.members.group;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.members.CannotModifyLockedClubOccupationException;
import fi.riista.feature.huntingclub.members.HuntingClubOccupationDTOTransformer;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationSort;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.Occupation_;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.feature.organization.person.PersonNotFoundException;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSpecs;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

@Component
public class GroupMemberCrudFeature extends AbstractCrudFeature<Long, Occupation, OccupationDTO> {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    protected PersonLookupService personLookupService;

    @Resource
    private HuntingLeaderCanExitGroupService huntingLeaderCanExitGroupService;

    @Resource
    private HuntingClubOccupationDTOTransformer clubOccupationDTOTransformer;

    @Resource
    protected OccupationRepository occupationRepository;

    @Resource
    protected HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Override
    protected JpaRepository<Occupation, Long> getRepository() {
        return occupationRepository;
    }

    @Override
    protected OccupationDTO toDTO(@Nonnull final Occupation entity) {
        return clubOccupationDTOTransformer.apply(entity);
    }

    @Transactional(readOnly = true)
    public boolean isLocked(final long occupationId) {
        return huntingLeaderCanExitGroupService.isHuntingLeaderLocked(occupationRepository.getOne(occupationId));
    }

    @Override
    @Transactional
    public OccupationDTO create(final OccupationDTO dto) {
        checkArgument(!occupationRepository.alreadyExists(dto), "Person already has occupation in group.");
        return super.create(dto);
    }

    @Override
    protected void delete(final Occupation occupation) {
        huntingLeaderCanExitGroupService.assertHuntingLeaderNotLocked(occupation);
        assertGroupLeaderCanBeModified(occupation.getOrganisation(), occupation.getOccupationType());

        final Organisation org = occupation.getOrganisation();

        if (!occupation.isDeleted()) {
            occupation.setEndDate(DateUtil.today());
            occupation.setCallOrder(null);
            occupation.softDelete();
        }

        updateExistingOccupationsContactOrder(org);
    }

    @Override
    protected void updateEntity(final Occupation entity, final OccupationDTO dto) {
        if (entity.isNew()) {
            final HuntingClubGroup group = huntingClubGroupRepository.getOne(dto.getOrganisationId());
            final Person person = personLookupService
                    .findById(dto.getPersonId(), Occupation.FOREIGN_PERSON_ELIGIBLE_FOR_OCCUPATION)
                    .orElseThrow(() -> PersonNotFoundException.byPersonId(dto.getPersonId()));

            entity.setOrganisationAndOccupationType(group, dto.getOccupationType());

            assertGroupLeaderIsAdult(person, dto.getOccupationType());
            assertGroupLeaderCanBeModified(group, dto.getOccupationType());

            entity.setPerson(person);
            entity.setBeginDate(group.getOrganisationType().getBeginDateForNewOccupation());
            entity.setCallOrder(calculateLastCallOrderValue(group, dto.getOccupationType()));

            // Always copy contact info share from club membership
            getContactInfoShare(group.getParentOrganisation(), person).ifPresent(entity::setContactInfoShare);
        }
    }

    private Optional<ContactInfoShare> getContactInfoShare(final Organisation club, final Person person) {
        final List<Occupation> activeClubRoles = occupationRepository.findActiveByOrganisationAndPerson(club, person);

        if (activeClubRoles.size() > 1) {
            throw new IllegalStateException(String.format("Unexpected occupations count for personId:%d clubId:%d count:%d",
                    person.getId(), club.getId(), activeClubRoles.size()));
        }

        return activeClubRoles.stream()
                .map(Occupation::getContactInfoShare)
                .filter(Objects::nonNull)
                .findFirst();
    }

    private Integer calculateLastCallOrderValue(final Organisation organisation,
                                                final OccupationType occupationType) {
        // Always set order to leader roles
        return occupationType != OccupationType.RYHMAN_METSASTYKSENJOHTAJA
                ? null
                : occupationRepository.countNotDeletedByTypeAndOrganisation(organisation.getId(), occupationType);
    }

    @Transactional
    public OccupationDTO updateOccupationType(final long id, final OccupationType occupationType) {
        final Occupation existingOccupation = requireEntity(id, EntityPermission.UPDATE);

        // changing occupation type to same is unnecessary
        if (Objects.equals(occupationType, existingOccupation.getOccupationType())) {
            return clubOccupationDTOTransformer.apply(existingOccupation);
        }

        huntingLeaderCanExitGroupService.assertHuntingLeaderNotLocked(existingOccupation);

        assertGroupLeaderIsAdult(existingOccupation.getPerson(), occupationType);
        assertGroupLeaderCanBeModified(existingOccupation.getOrganisation(), existingOccupation.getOccupationType());
        assertGroupLeaderCanBeModified(existingOccupation.getOrganisation(), occupationType);

        if (!existingOccupation.isDeleted()) {
            existingOccupation.setEndDate(DateUtil.today());
            existingOccupation.setCallOrder(null);
            existingOccupation.softDelete();
        }

        updateExistingOccupationsContactOrder(existingOccupation.getOrganisation());

        final Occupation newOccupation = new Occupation(
                existingOccupation.getPerson(), existingOccupation.getOrganisation(), occupationType);
        newOccupation.setBeginDate(existingOccupation.getBeginDate());
        newOccupation.setContactInfoShare(existingOccupation.getContactInfoShare());
        newOccupation.setCallOrder(calculateLastCallOrderValue(existingOccupation.getOrganisation(), occupationType));

        return clubOccupationDTOTransformer.apply(occupationRepository.saveAndFlush(newOccupation));
    }

    @Transactional(readOnly = true)
    public List<OccupationDTO> listMembers(final long groupId) {
        final HuntingClubGroup group = requireEntityService.requireHuntingGroup(groupId, EntityPermission.READ);

        final Comparator<Occupation> sort = OccupationSort.BY_TYPE
                .thenComparing(OccupationSort.BY_CALL_ORDER)
                .thenComparing(OccupationSort.BY_LAST_NAME)
                .thenComparing(OccupationSort.BY_BYNAME);

        final List<Occupation> occupations = occupationRepository.findActiveByOrganisation(group);

        return clubOccupationDTOTransformer.apply(occupations.stream().sorted(sort).collect(toList()));
    }

    private void updateExistingOccupationsContactOrder(Organisation org) {
        final List<Occupation> orderedExistingLeaders = occupationRepository.findAll(JpaSpecs.and(
                JpaSpecs.equal(Occupation_.organisation, org),
                JpaSpecs.equal(Occupation_.occupationType, OccupationType.RYHMAN_METSASTYKSENJOHTAJA),
                JpaSpecs.notSoftDeleted()
        ), JpaSort.of(Sort.Direction.ASC, Occupation_.callOrder));

        updateOccupationsContactOrder(orderedExistingLeaders);
    }

    @Transactional
    public void updateContactOrder(final long groupId, final List<Long> memberIds) {
        final HuntingClubGroup group = requireEntityService.requireHuntingGroup(groupId, EntityPermission.UPDATE);
        assertPermitIsNotLocked(group);
        final HuntingClub club = huntingClubRepository.getOne(group.getParentOrganisation().getId());
        userAuthorizationHelper.assertClubContactOrModerator(club);

        final List<Occupation> occupations = Lists.newArrayList(occupationRepository.findAllById(memberIds));
        final Ordering<HasID<Long>> orderByList = Ordering
                .explicit(memberIds).nullsLast()
                .onResultOf(F::getId);

        updateOccupationsContactOrder(orderByList.immutableSortedCopy(occupations));
    }

    private static void updateOccupationsContactOrder(final List<Occupation> orderedOccupations) {
        int callOrderCounter = 0;
        for (Occupation occupation : orderedOccupations) {
            occupation.setCallOrder(callOrderCounter++);
        }
    }

    private void assertGroupLeaderCanBeModified(final Organisation group, final OccupationType occType) {
        if (occType == OccupationType.RYHMAN_METSASTYKSENJOHTAJA) {
            final HuntingClub club = huntingClubRepository.getOne(group.getParentOrganisation().getId());
            userAuthorizationHelper.assertClubContactOrModerator(club);
            final HuntingClubGroup g = huntingClubGroupRepository.getOne(group.getId());
            assertPermitIsNotLocked(g);
        }
    }

    private void assertPermitIsNotLocked(HuntingClubGroup group) {
        if (!activeUserService.isModeratorOrAdmin() && harvestPermitLockedByDateService.isPermitLocked(group)) {
            throw new CannotModifyLockedClubOccupationException(String.format(
                    "Group attached to permit but permit %s locked for hunting year %d",
                    group.getHarvestPermit().getPermitNumber(),
                    group.getHuntingYear()));
        }
    }

    private static void assertGroupLeaderIsAdult(final Person person, final OccupationType occupationType) {
        if (occupationType == OccupationType.RYHMAN_METSASTYKSENJOHTAJA) {
            Preconditions.checkState(person.isAdult(), "hunting leader must be adult");
        }
    }
}
