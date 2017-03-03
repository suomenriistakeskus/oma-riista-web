package fi.riista.feature.harvestpermit;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import fi.riista.util.F;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

@Component
public class HarvestPermitAuthorization extends AbstractEntityAuthorization<HarvestPermit> {

    public enum Permission {
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
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    public HarvestPermitAuthorization() {
        allow(EntityPermission.READ,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.CONTACT_PERSON_FOR_PERMIT,
                Role.PERMIT_HOLDER,
                Role.PERMIT_PARTNER,
                Role.PERMIT_RHY_COORDINATOR,
                Role.PERMIT_RELATED_RHY_COORDINATOR);

        allow(EntityPermission.UPDATE,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.CONTACT_PERSON_FOR_PERMIT);

        allow(Permission.UPDATE_ALLOCATIONS,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.CONTACT_PERSON_FOR_PERMIT,
                Role.CONTACT_PERSON_OF_PERMIT_HOLDER,
                Role.HUNTING_LEADER_OF_PERMIT_HOLDER);

        allow(Permission.CREATE_REMOVE_MOOSE_HARVEST_REPORT,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.CONTACT_PERSON_FOR_PERMIT,
                Role.CONTACT_PERSON_OF_PERMIT_HOLDER,
                Role.HUNTING_LEADER_OF_PERMIT_HOLDER);

        allow(Permission.LIST_LEADERS,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.CONTACT_PERSON_FOR_PERMIT,
                Role.CONTACT_PERSON_OF_PERMIT_PARTNER,
                Role.CONTACT_PERSON_OF_PERMIT_HOLDER,
                Role.HUNTING_LEADER_OF_PERMIT_PARTNER,
                Role.HUNTING_LEADER_OF_PERMIT_HOLDER);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final HarvestPermit permit,
                                   @Nonnull final UserInfo userInfo) {
        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            // Luvan yhteyshenkilö
            collector.addAuthorizationRole(Role.CONTACT_PERSON_FOR_PERMIT, () ->
                    permit.hasContactPerson(activePerson));

            // Jäsen seurassa joka on luvan saaja
            collector.addAuthorizationRole(Role.PERMIT_HOLDER, () ->
                    isMemberOfPermitHolder(permit, activePerson));

            // Jäsen seurassa joka on luvan osakas
            collector.addAuthorizationRole(Role.PERMIT_PARTNER, () ->
                    isMemberOfPermitPartner(permit, activePerson));

            // Yhteyshenkilö luvan saajan seurassa
            collector.addAuthorizationRole(Role.CONTACT_PERSON_OF_PERMIT_HOLDER, () ->
                    isContactInPermitHolderClub(permit, activePerson));

            // Yhteyshenkilö luvan osakkaan seurassa
            collector.addAuthorizationRole(Role.CONTACT_PERSON_OF_PERMIT_PARTNER, () ->
                    isContactInPermitPartnerClub(permit, activePerson));

            // Metsästyksenjohtaja luvan osakkaan seurassa
            collector.addAuthorizationRole(Role.HUNTING_LEADER_OF_PERMIT_PARTNER, () ->
                    isLeaderInPermitPartnerClubGroup(permit, activePerson));

            // Metsästyksenjohtaja luvan saajan seurassa
            collector.addAuthorizationRole(Role.HUNTING_LEADER_OF_PERMIT_HOLDER, () ->
                    isLeaderInPermitHolderClubGroup(permit, activePerson));

            // Toiminnanohjaaja (tiedoksi)
            collector.addAuthorizationRole(Role.PERMIT_RELATED_RHY_COORDINATOR, () ->
                    isCoordinator(permit.getRelatedRhys()));

            // Toiminnanohjaaja
            collector.addAuthorizationRole(Role.PERMIT_RHY_COORDINATOR, () ->
                    isCoordinator(permit.getRhy(), permit.getHarvests()));
        });
    }

    private boolean isContactInPermitHolderClub(HarvestPermit permit, Person person) {
        return permit.getPermitHolder() != null && userAuthorizationHelper.hasRoleInOrganisation(
                permit.getPermitHolder(), person, OccupationType.SEURAN_YHDYSHENKILO);
    }

    private boolean isContactInPermitPartnerClub(HarvestPermit permit, Person person) {
        return permit.getPermitPartners() != null && userAuthorizationHelper.hasAnyOfRolesInOrganisations(
                permit.getPermitPartners(), person, EnumSet.of(OccupationType.SEURAN_YHDYSHENKILO));
    }

    private boolean isLeaderInPermitHolderClubGroup(HarvestPermit permit, Person person) {
        return permit.getPermitHolder() != null && userAuthorizationHelper.hasAnyOfRolesInOrganisations(
                huntingClubGroupRepository.findByPermitAndClubs(permit, singleton(permit.getPermitHolder())),
                person, EnumSet.of(OccupationType.RYHMAN_METSASTYKSENJOHTAJA));
    }

    private boolean isLeaderInPermitPartnerClubGroup(HarvestPermit permit, Person person) {
        return permit.getPermitPartners() != null && userAuthorizationHelper.hasAnyOfRolesInOrganisations(
                huntingClubGroupRepository.findByPermitAndClubs(permit, permit.getPermitPartners()),
                person, EnumSet.of(OccupationType.RYHMAN_METSASTYKSENJOHTAJA));
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

    private boolean isCoordinator(final Set<Riistanhoitoyhdistys> rhys) {
        return userAuthorizationHelper.isCoordinator(F.getUniqueIds(rhys));
    }
}
