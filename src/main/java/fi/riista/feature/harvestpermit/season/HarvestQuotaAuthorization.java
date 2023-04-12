package fi.riista.feature.harvestpermit.season;

import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_USER;
import static fi.riista.feature.account.user.SystemUserPrivilege.MODERATE_HARVEST_SEASONS;
import static fi.riista.feature.harvestpermit.season.HarvestSeasonAuthorization.Role.HARVEST_SEASON_MODERATOR;

@Component
public class HarvestQuotaAuthorization extends AbstractEntityAuthorization<HarvestQuota> {
    public HarvestQuotaAuthorization() {
        allow(EntityPermission.READ, ROLE_ADMIN, ROLE_MODERATOR, ROLE_USER);
        allow(EntityPermission.CREATE, ROLE_ADMIN, HARVEST_SEASON_MODERATOR);
        allow(EntityPermission.UPDATE, ROLE_ADMIN, HARVEST_SEASON_MODERATOR);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final HarvestQuota quota,
                                   @Nonnull final UserInfo userInfo) {
        collector.addAuthorizationRole(HarvestSeasonAuthorization.Role.HARVEST_SEASON_MODERATOR,
                () -> userInfo.hasPrivilege(MODERATE_HARVEST_SEASONS));
    }

}
