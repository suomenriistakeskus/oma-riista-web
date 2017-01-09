package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameDiaryEntryAuthorization;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import fi.riista.util.F;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class HarvestAuthorization extends GameDiaryEntryAuthorization<Harvest> {

    public enum HarvestPermission {
        LINK_HARVEST_TO_HUNTING_DAY_OF_GROUP
    }

    private enum Role {
        AUTHOR,
        ACTOR,
        PERMIT_CONTACT_PERSON,
        HARVEST_RHY_COORDINATOR,
    }

    public HarvestAuthorization() {
        super(Harvest.class);

        allow(CREATE,
                SystemUser.Role.ROLE_USER,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR);

        allow(READ,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.AUTHOR,
                Role.ACTOR,
                Role.HARVEST_RHY_COORDINATOR,
                Role.PERMIT_CONTACT_PERSON,
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_JASEN,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        allow(UPDATE,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.AUTHOR,
                Role.ACTOR,
                Role.PERMIT_CONTACT_PERSON,
                OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        allow(DELETE,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.AUTHOR,
                Role.ACTOR);

        allow(HarvestPermission.LINK_HARVEST_TO_HUNTING_DAY_OF_GROUP,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class[]{Harvest.class, HarvestDTO.class, MobileHarvestDTO.class};
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
                harvest.getHarvestPermit().isHarvestsAsList() &&
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
