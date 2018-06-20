package fi.riista.feature.gamediary.observation;

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
public class ObservationAuthorization extends GameDiaryEntryAuthorization<Observation> {

    public enum Permission {
        LINK_OBSERVATION_TO_HUNTING_DAY_OF_GROUP,
        // To fix incorrect location scanned from moose data card regardless of state of hunting
        FIX_GEOLOCATION
    }

    private enum Role {
        AUTHOR,
        ACTOR
    }

    public ObservationAuthorization() {
        allow(EntityPermission.CREATE, ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR);

        allow(EntityPermission.READ,
                ROLE_ADMIN,
                ROLE_MODERATOR,
                Role.AUTHOR,
                Role.ACTOR,
                OccupationType.SEURAN_JASEN,
                OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_JASEN,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        allow(EntityPermission.UPDATE,
                ROLE_ADMIN,
                ROLE_MODERATOR,
                Role.AUTHOR,
                Role.ACTOR,
                OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        allow(EntityPermission.DELETE, ROLE_ADMIN, ROLE_MODERATOR, Role.AUTHOR, Role.ACTOR);

        allow(Permission.LINK_OBSERVATION_TO_HUNTING_DAY_OF_GROUP,
                ROLE_ADMIN,
                ROLE_MODERATOR,
                OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        allow(Permission.FIX_GEOLOCATION, ROLE_ADMIN, ROLE_MODERATOR);
    }

    @Override
    protected void collectNonClubRoles(final Person person,
                                       final AuthorizationTokenCollector collector,
                                       final Observation observation) {
        collector.addAuthorizationRole(Role.AUTHOR, () -> Objects.equals(person.getId(), F.getId(observation.getAuthor())));
        collector.addAuthorizationRole(Role.ACTOR, () -> Objects.equals(person.getId(), F.getId(observation.getActor())));
    }
}
