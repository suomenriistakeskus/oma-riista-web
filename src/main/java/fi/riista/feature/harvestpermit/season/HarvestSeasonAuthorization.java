package fi.riista.feature.harvestpermit.season;

import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_USER;

@Component
public class HarvestSeasonAuthorization extends AbstractEntityAuthorization {

    public HarvestSeasonAuthorization() {
        super("HarvestSeason");

        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, ROLE_USER);
        allow(CREATE, ROLE_ADMIN);
        allow(UPDATE, ROLE_ADMIN);
    }

    @Override
    protected void authorizeTarget(AuthorizationTokenCollector collector, EntityAuthorizationTarget target, UserInfo userInfo) {
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class<?>[]{HarvestSeason.class, HarvestSeasonDTO.class};
    }
}
