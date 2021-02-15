package fi.riista.feature.account.user;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup_;
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
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static fi.riista.feature.organization.occupation.OccupationType.PETOYHDYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.SRVA_YHTEYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.notSoftDeleted;
import static fi.riista.util.jpa.JpaSpecs.overlapsInterval;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.joining;
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
        activeUserService.findActiveUserInfo()
                .filter(userInfo -> userInfo.isAdminOrModerator() || isCoordinatorAnywhere(userInfo))
                .orElseThrow(() -> new AccessDeniedException(String.format(
                        "User id:%s is not coordinator anywhere", activeUserService.getActiveUserIdOrNull())));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void assertCoordinatorOrModerator(final long rhyId) {
        activeUserService.findActiveUserInfo()
                .filter(userInfo -> userInfo.isAdminOrModerator() || isCoordinator(singleton(rhyId), userInfo))
                .orElseThrow(() -> new AccessDeniedException(String.format(
                        "User id:%s is not coordinator for rhyId:%s",
                        activeUserService.getActiveUserIdOrNull(), rhyId)));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void assertClubContactOrModerator(final HuntingClub club) {
        activeUserService.findActiveUserInfo()
                .filter(userInfo -> {
                    return userInfo.isAdminOrModerator()
                            || getPerson(userInfo)
                                    .map(person -> isClubContact(club, person))
                                    .orElse(false);
                })
                .orElseThrow(() -> new AccessDeniedException(String.format(
                        "User id:%s is not contact for clubId:%s",
                        activeUserService.getActiveUserIdOrNull(), club.getId())));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isCoordinator(final Organisation rhy) {
        return isCoordinator(rhy, activeUserService.getActiveUserInfoOrNull());
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isCoordinator(final Organisation rhy, final UserInfo userInfo) {
        return rhy != null && getPerson(userInfo)
                .map(person -> isCoordinator(rhy, person))
                .orElse(false);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isCoordinator(final Collection<Long> rhyIds) {
        return !rhyIds.isEmpty() && isCoordinator(rhyIds, activeUserService.getActiveUserInfoOrNull());
    }

    private boolean isCoordinator(final Collection<Long> rhyIds, final UserInfo userInfo) {
        return getPerson(userInfo)
                .map(person -> hasAnyOfRolesInOrganisationIds(rhyIds, person, EnumSet.of(TOIMINNANOHJAAJA)))
                .orElse(false);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isCoordinator(final Organisation rhy, final Person person) {
        return hasRoleInOrganisation(rhy, person, TOIMINNANOHJAAJA);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isSrvaContactPerson(final Organisation rhy, final Person person) {
        return rhy != null && person != null && hasRoleInOrganisation(rhy, person, SRVA_YHTEYSHENKILO);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasRoleInOrganisation(
            final Organisation organisation, final Person person, final OccupationType role) {

        return hasAnyOfRolesInOrganisation(organisation, person, EnumSet.of(role));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasAnyOfRolesInOrganisation(
            final Organisation organisation, final Person person, final Set<OccupationType> roles) {

        final Set<OccupationType> applicableRoles = roles.stream()
                .filter(occType -> checkApplicabilityOfRole(occType, organisation))
                .collect(toSet());

        return !applicableRoles.isEmpty()
                && hasAnyOfRolesInOrganisationIds(singleton(organisation.getId()), person, applicableRoles);
    }

    private static boolean checkApplicabilityOfRole(final OccupationType occType, final Organisation organisation) {
        final OrganisationType orgType = organisation.getOrganisationType();
        final boolean applicable = occType.isApplicableFor(orgType);

        if (!applicable) {
            LOG.warn("Incorrect role requested for {} organisation with ID={}: {}",
                    orgType.name(), organisation.getId(), occType);
        }

        return applicable;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public <T extends Organisation> boolean hasAnyOfRolesInOrganisations(final Collection<T> organisations,
                                                                         final Person person,
                                                                         final Set<OccupationType> roles) {
        if (organisations.isEmpty() || roles.isEmpty()) {
            return false;
        }

        final List<Organisation> applicableOrganisations = organisations.stream().filter(org -> {
            final OrganisationType orgType = org.getOrganisationType();
            final boolean applicable = roles.stream().anyMatch(occType -> occType.isApplicableFor(orgType));

            if (!applicable) {
                LOG.warn("Incorrect usage of hasAnyOfRolesInOrganisations: inapplicable roles requested " +
                        "for {} organisation with ID={}: {}", orgType.name(), org.getId(), roles);
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
                                filteredOrgTypes.stream().map(Enum::name).collect(joining(", ")), occType.name());
                    }

                    return applicable;
                })
                .collect(toSet());

        return !applicableRoles.isEmpty() &&
                hasAnyOfRolesInOrganisationIds(F.getUniqueIds(applicableOrganisations), person, applicableRoles);
    }

    private boolean hasAnyOfRolesInOrganisationIds(
            final Collection<Long> organisationIds, final Person person, final Set<OccupationType> roles) {

        return occupationRepository.countActiveByTypeAndPersonAndOrganizationIn(organisationIds, person, roles) > 0;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Set<OccupationType> findValidRolesInOrganisation(final Organisation organisation, final Person person) {
        return F.mapNonNullsToSet(
                occupationRepository.findActiveByOrganisationAndPerson(organisation, person),
                Occupation::getOccupationType);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<Long, List<Occupation>> findActiveOccupationsInOrganisations(@Nonnull final Collection<Long> organisationIds,
                                                                            @Nonnull final Person person,
                                                                            @Nullable final DateTime beginTime,
                                                                            @Nullable final DateTime endTime) {

        final Specification<Occupation> constraint = Specification
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
        return hasRoleAnywhere(person, TOIMINNANOHJAAJA);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isSrvaContactPersonAnywhere(final Person person) {
        return hasRoleAnywhere(person, SRVA_YHTEYSHENKILO);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isCarnivoreContactPersonAnywhere(final LocalDate date) {
        return getPerson(activeUserService.getActiveUserInfoOrNull())
                .map(person -> isCarnivoreContactPersonAnywhere(person, date))
                .orElse(false);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isCarnivoreContactPersonAnywhere(final Person person, final LocalDate date) {
        final EnumSet<OccupationType> roles = EnumSet.of(PETOYHDYSHENKILO);
        return occupationRepository.countActiveByPersonAndTypesAndValidOn(person, roles, date) > 0;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasRoleAnywhere(final Person person, final OccupationType role) {
        return person != null && hasAnyOfRolesAnywhere(person, EnumSet.of(role));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasAnyOfRolesAnywhere(final Person person, final EnumSet<OccupationType> roles) {
        return !roles.isEmpty() && occupationRepository.countActiveByTypeAndPerson(person, roles) > 0;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isClubMember(final Organisation club, final Person person) {
        return hasRoleInOrganisation(club, person, SEURAN_JASEN);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isClubContact(final Organisation club, final Person person) {
        return hasRoleInOrganisation(club, person, SEURAN_YHDYSHENKILO);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isGroupMember(final Organisation group, final Person person) {
        return hasRoleInOrganisation(group, person, RYHMAN_JASEN);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isGroupLeader(final Organisation group, final Person person) {
        return hasRoleInOrganisation(group, person, RYHMAN_METSASTYKSENJOHTAJA);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isLeaderOfSomePermitHuntingGroup(
            final Person person, final HarvestPermit permit, final HuntingClub club, final GameSpecies species, final int huntingYear) {

        final Specification<HuntingClubGroup> compoundGroupCriteria = Specification
                .where(equal(HuntingClubGroup_.species, species))
                .and(equal(HuntingClubGroup_.huntingYear, huntingYear))
                .and(equal(Organisation_.parentOrganisation, club))
                .and(JpaSubQuery.inverseOf(HarvestPermit_.permitGroups).exists((root, cb) -> cb.equal(root, permit)));

        return hasAnyOfRolesInOrganisations(
                huntingGroupRepository.findAll(compoundGroupCriteria),
                person,
                EnumSet.of(RYHMAN_METSASTYKSENJOHTAJA));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Optional<Person> getPerson(@Nullable final UserInfo userInfo) {
        return Optional.ofNullable(userInfo)
                .map(UserInfo::getUserId)
                .flatMap(userRepository::findById)
                .map(SystemUser::getPerson);
    }
}
