package fi.riista.feature.organization.rhy;

import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SRVA_YHTEYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission.LIST_SRVA;
import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission.VIEW_SHOOTING_TEST_EVENTS;
import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission.VIEW_SHOOTING_TEST_EVENTS_BY_YEAR;
import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission.LIST_HUNTING_CONTROL_EVENTS;
import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission.LIST_GAME_DAMAGE_INSPECTION_EVENTS;
import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyModeratorPermission.CREATE_NOMINATION_DECISION;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

@Component
public class RiistanhoitoyhdistysAuthorization extends AbstractEntityAuthorization<Riistanhoitoyhdistys> {

    public enum RhyPermission {
        LIST_SRVA,
        VIEW_SHOOTING_TEST_EVENTS,
        VIEW_SHOOTING_TEST_EVENTS_BY_YEAR,
        LIST_HUNTING_CONTROL_EVENTS,
        LIST_GAME_DAMAGE_INSPECTION_EVENTS
    }

    public enum RhyModeratorPermission {
        CREATE_NOMINATION_DECISION
    }

    @Resource
    private RhyAuthorizationHelper helper;

    public RiistanhoitoyhdistysAuthorization() {
        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(LIST_SRVA, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, SRVA_YHTEYSHENKILO);
        allow(VIEW_SHOOTING_TEST_EVENTS, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, AMPUMAKOKEEN_VASTAANOTTAJA);
        allow(VIEW_SHOOTING_TEST_EVENTS_BY_YEAR, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(LIST_HUNTING_CONTROL_EVENTS, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(LIST_GAME_DAMAGE_INSPECTION_EVENTS, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(CREATE_NOMINATION_DECISION, ROLE_ADMIN, ROLE_MODERATOR);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final Riistanhoitoyhdistys rhy,
                                   @Nonnull final UserInfo userInfo) {

        helper.collectAllRhyRoles(rhy, collector, userInfo);
    }
}
