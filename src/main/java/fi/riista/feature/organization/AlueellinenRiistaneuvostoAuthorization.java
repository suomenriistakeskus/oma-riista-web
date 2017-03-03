package fi.riista.feature.organization;

import fi.riista.security.EntityPermission;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import org.springframework.stereotype.Component;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;

@Component
public class AlueellinenRiistaneuvostoAuthorization extends AbstractEntityAuthorization<AlueellinenRiistaneuvosto> {
    public AlueellinenRiistaneuvostoAuthorization() {
        allow(EntityPermission.READ, ROLE_ADMIN, ROLE_MODERATOR);
        allow(EntityPermission.UPDATE, ROLE_ADMIN, ROLE_MODERATOR);
    }
}
