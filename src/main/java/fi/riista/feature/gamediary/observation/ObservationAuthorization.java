package fi.riista.feature.gamediary.observation;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameDiaryEntryAuthorization;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import fi.riista.util.F;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ObservationAuthorization extends GameDiaryEntryAuthorization<Observation> {

    public enum Permission {
        LINK_OBSERVATION_TO_HUNTING_DAY_OF_GROUP
    }

    private enum Role {
        AUTHOR,
        ACTOR
    }

    public ObservationAuthorization() {
        allow(EntityPermission.CREATE,
                SystemUser.Role.ROLE_USER,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR);

        allow(EntityPermission.READ,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.AUTHOR,
                Role.ACTOR,
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_JASEN,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        allow(EntityPermission.UPDATE,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.AUTHOR,
                Role.ACTOR,
                OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        allow(EntityPermission.DELETE,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                Role.AUTHOR,
                Role.ACTOR);

        allow(Permission.LINK_OBSERVATION_TO_HUNTING_DAY_OF_GROUP,
                SystemUser.Role.ROLE_ADMIN,
                SystemUser.Role.ROLE_MODERATOR,
                OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
    }

    @Override
    protected void collectNonClubRoles(final Person person,
                                       final AuthorizationTokenCollector collector,
                                       final Observation observation) {
        collector.addAuthorizationRole(Role.AUTHOR, () -> Objects.equals(person.getId(), F.getId(observation.getAuthor())));
        collector.addAuthorizationRole(Role.ACTOR, () -> Objects.equals(person.getId(), F.getId(observation.getActor())));
    }
}
