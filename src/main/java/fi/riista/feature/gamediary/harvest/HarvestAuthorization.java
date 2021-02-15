package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.gamediary.GameDiaryEntryAuthorization;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import fi.riista.util.F;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_USER;

@Component
public class HarvestAuthorization extends GameDiaryEntryAuthorization<Harvest> {

    public enum Permission {
        LINK_HARVEST_TO_HUNTING_DAY_OF_GROUP,
        // To fix incorrect location scanned from moose data card regardless of state of hunting
        FIX_GEOLOCATION
    }

    private enum Role {
        AUTHOR,
        ACTOR,
        PERMIT_CONTACT_PERSON,
        HARVEST_RHY_COORDINATOR,
    }

    public HarvestAuthorization() {
        allow(EntityPermission.CREATE, ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR);

        allow(EntityPermission.READ,
                ROLE_ADMIN,
                ROLE_MODERATOR,
                Role.AUTHOR,
                Role.ACTOR,
                Role.PERMIT_CONTACT_PERSON,
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_JASEN,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        allow(EntityPermission.UPDATE,
                ROLE_ADMIN,
                ROLE_MODERATOR,
                Role.AUTHOR,
                Role.ACTOR,
                Role.PERMIT_CONTACT_PERSON,
                OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        allow(EntityPermission.DELETE, Role.AUTHOR, Role.ACTOR);

        allow(Permission.LINK_HARVEST_TO_HUNTING_DAY_OF_GROUP,
                ROLE_ADMIN,
                ROLE_MODERATOR,
                OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        allow(Permission.FIX_GEOLOCATION, ROLE_ADMIN, ROLE_MODERATOR);
    }

    @Override
    protected void collectNonClubRoles(final Person person,
                                       final AuthorizationTokenCollector collector,
                                       final Harvest harvest) {
        collector.addAuthorizationRole(Role.AUTHOR, () -> Objects.equals(person.getId(), F.getId(harvest.getAuthor())));
        collector.addAuthorizationRole(Role.ACTOR, () -> Objects.equals(person.getId(), F.getId(harvest.getActor())));
        collector.addAuthorizationRole(Role.PERMIT_CONTACT_PERSON, () -> isPermitContactPerson(person, harvest));
        collector.addAuthorizationRole(Role.HARVEST_RHY_COORDINATOR, () -> isCoordinatorInHarvestPermitRhy(person, harvest));
        collector.addAuthorizationRole(Role.HARVEST_RHY_COORDINATOR, () -> isCoordinatorInRhy(person, harvest));
    }

    private static boolean isPermitContactPerson(final Person person, final Harvest harvest) {
        return harvest.getHarvestPermit() != null &&
                harvest.getHarvestPermit().hasContactPerson(person);
    }

    private boolean isCoordinatorInRhy(final Person person, final Harvest gameDiaryEntry) {
        return gameDiaryEntry.getRhy() != null &&
                userAuthorizationHelper.isCoordinator(gameDiaryEntry.getRhy(), person);
    }

    private boolean isCoordinatorInHarvestPermitRhy(final Person person, final Harvest harvest) {
        return harvest.getHarvestPermit() != null &&
                userAuthorizationHelper.isCoordinator(harvest.getHarvestPermit().getRhy(), person);
    }
}
