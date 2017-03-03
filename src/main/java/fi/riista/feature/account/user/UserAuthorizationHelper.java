package fi.riista.feature.account.user;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.Occupation_;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.UserInfo;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaGroupingUtils;
import fi.riista.util.jpa.JpaSubQuery;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.notSoftDeleted;
import static fi.riista.util.jpa.JpaSpecs.overlapsInterval;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Component
public class UserAuthorizationHelper {

    private static final Logger LOG = LoggerFactory.getLogger(UserAuthorizationHelper.class);

    @Resource
    private UserRepository userRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private HuntingClubGroupRepository huntingGroupRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void assertCoordinatorAnywhereOrModerator() {
        if (!activeUserService.isModeratorOrAdmin() && !isCoordinatorAnywhere(activeUserService.getActiveUserInfo())) {
            throw new AccessDeniedException(String.format("User id:%s is not coordinator anywhere",
                    activeUserService.getActiveUserId()));
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void assertCoordinatorOrModerator(final long rhyId) {
        if (!activeUserService.isModeratorOrAdmin() && !isCoordinator(rhyId)) {
            throw new AccessDeniedException(String.format("User id:%s is not coordinator for rhyId:%s",
                    activeUserService.getActiveUserId(), rhyId));
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void assertClubContactOrModerator(final HuntingClub club) {
        if (activeUserService.isModeratorOrAdmin()) {
            return;
        }

        final Optional<Person> maybePerson = getPerson(activeUserService.getActiveUserInfo());

        if (!maybePerson.isPresent() || !isClubContact(club, maybePerson.get())) {
            throw new AccessDeniedException(String.format("User id:%s is not contact for clubId:%s",
                    activeUserService.getActiveUserId(), club.getId()));
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isCoordinator(final long rhyId) {
        return isCoordinator(Collections.singleton(rhyId), activeUserService.getActiveUserInfo());
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isCoordinator(final Organisation rhy) {
        return isCoordinator(rhy, activeUserService.getActiveUserInfo());
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isCoordinator(final Organisation rhy, final UserInfo userInfo) {
        return rhy != null && getPerson(userInfo)
                .map(person -> isCoordinator(rhy, person))
                .orElse(false);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isCoordinator(final Collection<Long> rhyIds) {
        return isCoordinator(rhyIds, activeUserService.getActiveUserInfo());
    }

    private boolean isCoordinator(final Collection<Long> rhyIds, final UserInfo userInfo) {
        return getPerson(userInfo)
                .map(person -> isCoordinator(rhyIds, person))
                .orElse(false);
    }

    private boolean isCoordinator(final Collection<Long> rhyIds, final Person person) {
        return hasAnyOfRolesInOrganisationIds(rhyIds, person, EnumSet.of(OccupationType.TOIMINNANOHJAAJA));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isCoordinator(final Organisation rhy, final Person person) {
        return hasAnyOfRolesInOrganisation(rhy, person, EnumSet.of(OccupationType.TOIMINNANOHJAAJA));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isSrvaContactPerson(final long rhyId) {
        return getPerson(activeUserService.getActiveUserInfo())
                .map(person -> isSrvaContactPerson(Collections.singleton(rhyId), person))
                .orElse(false);
    }

    private boolean isSrvaContactPerson(final Collection<Long> rhyIds, final Person person) {
        return hasAnyOfRolesInOrganisationIds(rhyIds, person, EnumSet.of(OccupationType.SRVA_YHTEYSHENKILO));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isSrvaContactPerson(final Organisation rhy, final UserInfo userInfo) {
        return rhy != null && getPerson(userInfo)
                .map(person -> hasRoleInOrganisation(rhy, person, OccupationType.SRVA_YHTEYSHENKILO))
                .orElse(false);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasRoleInOrganisation(
            final Organisation organisation, final Person person, final OccupationType role) {

        return hasAnyOfRolesInOrganisation(organisation, person, EnumSet.of(role));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasAnyOfRolesInOrganisation(
            final Organisation organisation, final Person person, final Set<OccupationType> roles) {

        final OrganisationType orgType = organisation.getOrganisationType();

        final Set<OccupationType> applicableRoles = roles.stream()
                .filter(occType -> {
                    final boolean applicable = occType.isApplicableFor(orgType);

                    if (!applicable) {
                        LOG.warn("Incorrect usage of hasAnyOfRolesInOrganisation: inapplicable role requested for " +
                                "organisation with ID={}, type={}: {}", organisation.getId(), orgType.name(), occType);
                    }

                    return applicable;
                })
                .collect(toSet());

        return !applicableRoles.isEmpty() &&
                hasAnyOfRolesInOrganisationIds(Collections.singleton(organisation.getId()), person, roles);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public <T extends Organisation> boolean hasAnyOfRolesInOrganisations(
            final Collection<T> organisations, final Person person, final Set<OccupationType> roles) {

        if (organisations.isEmpty() || roles.isEmpty()) {
            return false;
        }

        final List<Organisation> applicableOrganisations = organisations.stream().filter(org -> {
            final boolean applicable =
                    roles.stream().anyMatch(occType -> occType.isApplicableFor(org.getOrganisationType()));

            if (!applicable) {
                LOG.warn("Incorrect usage of hasAnyOfRolesInOrganisations: inapplicable roles requested " +
                                "for {} organisation with ID={}: {}",
                        org.getOrganisationType().name(), org.getId(), roles);
            }

            return applicable;
        }).collect(toList());

        if (applicableOrganisations.isEmpty()) {
            return false;
        }

        final Set<OrganisationType> filteredOrgTypes = applicableOrganisations.stream()
                .map(Organisation::getOrganisationType)
                .collect(toSet());

        final Set<OccupationType> applicableRoles = roles.stream()
                .filter(occType -> {
                    final boolean applicable =
                            occType.getApplicableOrganisationTypes().stream().anyMatch(filteredOrgTypes::contains);

                    if (!applicable) {
                        LOG.warn("Incorrect usage of hasAnyOfRolesInOrganisations: inapplicable role requested " +
                                        "for organisations of types [{}]: {}",
                                F.join(filteredOrgTypes, Enum::name, ", "), occType.name());
                    }

                    return applicable;
                })
                .collect(toSet());

        return !applicableRoles.isEmpty() &&
                hasAnyOfRolesInOrganisationIds(F.getUniqueIds(applicableOrganisations), person, applicableRoles);
    }

    private boolean hasAnyOfRolesInOrganisationIds(
            final Collection<Long> organisationIds, final Person person, final Set<OccupationType> roles) {

        return !organisationIds.isEmpty() &&
                !roles.isEmpty() &&
                occupationRepository.countActiveOccupationByTypeAndPersonAndOrganizationIn(
                        organisationIds, person, roles) > 0;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Set<OccupationType> findValidRolesInOrganisation(final Organisation organisation, final Person person) {
        return F.mapNonNullsToSet(
                occupationRepository.findActiveByOrganisationAndPerson(organisation, person),
                Occupation::getOccupationType);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<Long, List<Occupation>> findActiveOccupationsInOrganisations(
            @Nonnull final Collection<Long> organisationIds,
            @Nonnull final Person person,
            @Nullable final DateTime beginTime,
            @Nullable final DateTime endTime) {

        final Specification<Occupation> constraint = Specifications
                .where(overlapsInterval(Occupation_.beginDate, Occupation_.endDate, beginTime, endTime))
                .and(equal(Occupation_.person, person))
                .and(notSoftDeleted());

        return JpaGroupingUtils.groupRelationsById(
                organisationIds, Occupation_.organisation, Organisation_.id, occupationRepository, constraint);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isCoordinatorAnywhere(final UserInfo userInfo) {
        return getPerson(userInfo).map(this::isCoordinatorAnywhere).orElse(false);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isCoordinatorAnywhere(final Person person) {
        return hasRoleAnywhere(person, OccupationType.TOIMINNANOHJAAJA);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isSrvaContactPersonAnywhere(final UserInfo userInfo) {
        return getPerson(userInfo)
                .map(person -> hasRoleAnywhere(person, OccupationType.SRVA_YHTEYSHENKILO))
                .orElse(false);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasRoleAnywhere(final Person person, final OccupationType role) {
        return hasAnyOfRolesAnywhere(person, EnumSet.of(role));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasAnyOfRolesAnywhere(final Person person, final EnumSet<OccupationType> roles) {
        return !roles.isEmpty() && occupationRepository.countActiveOccupationByTypeAndPerson(person, roles) > 0;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isClubMember(final Organisation club, final Person person) {
        return hasRoleInOrganisation(club, person, OccupationType.SEURAN_JASEN);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isClubContact(final Organisation club, final Person person) {
        return hasRoleInOrganisation(club, person, OccupationType.SEURAN_YHDYSHENKILO);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isGroupMember(final Organisation group, final Person person) {
        return hasRoleInOrganisation(group, person, OccupationType.RYHMAN_JASEN);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isGroupLeader(final Organisation group, final Person person) {
        return hasRoleInOrganisation(group, person, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isLeaderOfSomePermitHuntingGroup(
            final Person person, final HarvestPermit permit, final HuntingClub club) {

        final Specification<HuntingClubGroup> clubCriteria = equal(Organisation_.parentOrganisation, club);
        final Specification<HuntingClubGroup> compoundGroupCriteria = Specifications
                .where(JpaSubQuery.inverseOf(HarvestPermit_.permitGroups).exists((root, cb) -> cb.equal(root, permit)))
                .and(clubCriteria);

        return hasAnyOfRolesInOrganisations(
                huntingGroupRepository.findAll(compoundGroupCriteria),
                person,
                EnumSet.of(OccupationType.RYHMAN_METSASTYKSENJOHTAJA));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Optional<Person> getPerson(final UserInfo userInfo) {
        return Optional.ofNullable(userInfo)
                .map(UserInfo::getUserId)
                .map(userRepository::findOne)
                .map(SystemUser::getPerson);
    }
}
