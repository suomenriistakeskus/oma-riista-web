package fi.riista.feature.shootingtest;

import org.springframework.stereotype.Component;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.feature.shootingtest.AbstractShootingTestEntityAuthorization.Role.ASSIGNED_SHOOTING_TEST_OFFICIAL;
import static fi.riista.feature.shootingtest.ShootingTestEventAuthorization.ShootingTestEventPermission.ASSIGN_OFFICIALS;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

@Component
public class ShootingTestEventAuthorization extends AbstractShootingTestEntityAuthorization<ShootingTestEvent> {

    public enum ShootingTestEventPermission {
        ASSIGN_OFFICIALS
    }

    public ShootingTestEventAuthorization() {
        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, AMPUMAKOKEEN_VASTAANOTTAJA);
        allow(CREATE, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, AMPUMAKOKEEN_VASTAANOTTAJA);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, ASSIGNED_SHOOTING_TEST_OFFICIAL);
        allow(ASSIGN_OFFICIALS, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, AMPUMAKOKEEN_VASTAANOTTAJA);
    }

    @Override
    protected ShootingTestEvent getShootingTestEvent(final ShootingTestEvent entity) {
        return entity;
    }
}
