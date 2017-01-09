package fi.riista.feature.harvestpermit;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import fi.riista.util.F;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

@Component
public class HarvestPermitAuthorization extends AbstractEntityAuthorization {

    public enum HarvestPermitPermission {
        LIST_LEADERS,
        UPDATE_ALLOCATIONS,
        CREATE_REMOVE_MOOSE_HARVEST_REPORT
    }

    public enum Role {
        CONTACT_PERSON_FOR_PERMIT,
        CONTACT_PERSON_OF_PERMIT_PARTNER,
        CONTACT_PERSON_OF_PERMIT_HOLDER,
        PERMIT_RHY_COORDINATOR,
        PERMIT_RELATED_RHY_COORDINATOR,
        PERMIT_HOLDER,
        PERMIT_PARTNER,
        HUNTING_LEADER_OF_PERMIT_PARTNER,
        HUNTING_LEADER_OF_PERMIT_HOLDER
    }

    @Resource
    private UserRepository userRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    public HarvestPermitAuthorization() {
        super("HarvestPermit");

        allow(READ,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.CONTACT_PERSON_FOR_PERMIT,
                Role.PERMIT_HOLDER,
                Role.PERMIT_PARTNER,
                Role.PERMIT_RHY_COORDINATOR,
                Role.PERMIT_RELATED_RHY_COORDINATOR);

        allow(UPDATE,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.CONTACT_PERSON_FOR_PERMIT);

        allow(HarvestPermitPermission.UPDATE_ALLOCATIONS,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.CONTACT_PERSON_FOR_PERMIT,
                Role.CONTACT_PERSON_OF_PERMIT_HOLDER,
                Role.HUNTING_LEADER_OF_PERMIT_HOLDER);

        allow(HarvestPermitPermission.CREATE_REMOVE_MOOSE_HARVEST_REPORT,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.CONTACT_PERSON_FOR_PERMIT,
                Role.CONTACT_PERSON_OF_PERMIT_HOLDER,
                Role.HUNTING_LEADER_OF_PERMIT_HOLDER);

        allow(HarvestPermitPermission.LIST_LEADERS,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.CONTACT_PERSON_FOR_PERMIT,
                Role.CONTACT_PERSON_OF_PERMIT_PARTNER,
                Role.CONTACT_PERSON_OF_PERMIT_HOLDER,
                Role.HUNTING_LEADER_OF_PERMIT_PARTNER,
                Role.HUNTING_LEADER_OF_PERMIT_HOLDER);
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(final HarvestPermit harvestPermit,
                                 final Person person,
                                 final Object permission) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final AuthorizationTokenCollector tokenCollector = createCollectorForPermission(authentication, permission);

        if (tokenCollector.hasPermission()) {
            return true;
        } else if (person != null) {
            authorize(tokenCollector, harvestPermit, person);
            return tokenCollector.hasPermission();
        } else {
            return false;
        }
    }

    @Override
    protected void authorizeTarget(final AuthorizationTokenCollector collector,
                                   final EntityAuthorizationTarget target,
                                   final UserInfo userInfo) {
        final SystemUser user = getSystemUser(userInfo);
        final HarvestPermit permit = getHarvestPermit(target);

        if (permit != null && user != null && user.getPerson() != null) {
            authorize(collector, permit, user.getPerson());
        }
    }

    private void authorize(final AuthorizationTokenCollector collector,
                           final HarvestPermit permit,
                           final Person person) {
        // Luvan yhteyshenkilö
        collector.addAuthorizationRole(Role.CONTACT_PERSON_FOR_PERMIT, () -> permit.hasContactPerson(person));

        // Jäsen seurassa joka on luvan saaja
        collector.addAuthorizationRole(Role.PERMIT_HOLDER, () -> isMemberOfPermitHolder(permit, person));

        // Jäsen seurassa joka on luvan osakas
        collector.addAuthorizationRole(Role.PERMIT_PARTNER, () -> isMemberOfPermitPartner(permit, person));

        // Yhteyshenkilö luvan saajan seurassa
        collector.addAuthorizationRole(Role.CONTACT_PERSON_OF_PERMIT_HOLDER,
                () -> isContactInPermitHolderClub(permit, person));

        // Yhteyshenkilö luvan osakkaan seurassa
        collector.addAuthorizationRole(Role.CONTACT_PERSON_OF_PERMIT_PARTNER,
                () -> isContactInPermitPartnerClub(permit, person));

        // Metsästyksenjohtaja luvan osakkaan seurassa
        collector.addAuthorizationRole(Role.HUNTING_LEADER_OF_PERMIT_PARTNER,
                () -> isLeaderInPermitPartnerClubGroup(permit, person));

        // Metsästyksenjohtaja luvan saajan seurassa
        collector.addAuthorizationRole(Role.HUNTING_LEADER_OF_PERMIT_HOLDER,
                () -> isLeaderInPermitHolderClubGroup(permit, person));

        // Toiminnanohjaaja (tiedoksi)
        collector.addAuthorizationRole(Role.PERMIT_RELATED_RHY_COORDINATOR,
                () -> isCoordinator(permit.getRelatedRhys()));

        // Toiminnanohjaaja
        collector.addAuthorizationRole(Role.PERMIT_RHY_COORDINATOR,
                () -> isCoordinator(permit.getRhy(), permit.getHarvests()));
    }

    private boolean isContactInPermitHolderClub(HarvestPermit permit, Person person) {
        return permit.getPermitHolder() != null && userAuthorizationHelper.hasRoleInOrganisation(
                permit.getPermitHolder(), person, OccupationType.SEURAN_YHDYSHENKILO);
    }

    private boolean isContactInPermitPartnerClub(HarvestPermit permit, Person person) {
        return permit.getPermitPartners() != null && userAuthorizationHelper.hasAnyOfRolesInOrganisations(
                permit.getPermitPartners(), person,
                EnumSet.of(OccupationType.SEURAN_YHDYSHENKILO));
    }

    private boolean isLeaderInPermitHolderClubGroup(HarvestPermit permit, Person person) {
        return permit.getPermitHolder() != null && userAuthorizationHelper.hasAnyOfRolesInOrganisations(
                huntingClubGroupRepository.findByPermitAndClubs(permit, singleton(permit.getPermitHolder())),
                person,
                EnumSet.of(OccupationType.RYHMAN_METSASTYKSENJOHTAJA));
    }

    private boolean isLeaderInPermitPartnerClubGroup(HarvestPermit permit, Person person) {
        return permit.getPermitPartners() != null && userAuthorizationHelper.hasAnyOfRolesInOrganisations(
                huntingClubGroupRepository.findByPermitAndClubs(permit, permit.getPermitPartners()),
                person,
                EnumSet.of(OccupationType.RYHMAN_METSASTYKSENJOHTAJA));
    }

    private boolean isMemberOfPermitHolder(HarvestPermit permit, Person person) {
        return permit.getPermitHolder() != null && userAuthorizationHelper.hasAnyOfRolesInOrganisation(
                permit.getPermitHolder(), person,
                EnumSet.of(OccupationType.SEURAN_JASEN, OccupationType.SEURAN_YHDYSHENKILO));
    }

    private boolean isMemberOfPermitPartner(HarvestPermit permit, Person person) {
        return permit.getPermitPartners() != null && userAuthorizationHelper.hasAnyOfRolesInOrganisations(
                permit.getPermitPartners(), person,
                EnumSet.of(OccupationType.SEURAN_JASEN, OccupationType.SEURAN_YHDYSHENKILO));
    }

    private boolean isCoordinator(final Riistanhoitoyhdistys permitRhy, final Set<Harvest> harvests) {
        final Set<Riistanhoitoyhdistys> rhys = harvests.stream().map(Harvest::getRhy).collect(toSet());
        rhys.add(permitRhy);
        return isCoordinator(rhys);
    }

    private boolean isCoordinator(Set<Riistanhoitoyhdistys> rhys) {
        return userAuthorizationHelper.isCoordinator(F.getUniqueIds(rhys));
    }

    private HarvestPermit getHarvestPermit(final EntityAuthorizationTarget target) {
        return Optional.ofNullable((Long) target.getAuthorizationTargetId())
                .map(harvestPermitRepository::getOne)
                .orElse(null);
    }

    private SystemUser getSystemUser(final UserInfo userInfo) {
        return Optional.ofNullable(userInfo.getUserId()).map(userRepository::getOne).orElse(null);
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class<?>[]{HarvestPermit.class, HarvestPermitDTO.class};
    }
}
