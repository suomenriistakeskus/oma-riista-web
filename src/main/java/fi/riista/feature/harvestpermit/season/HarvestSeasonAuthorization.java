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

@Component
public class HarvestSeasonAuthorization extends AbstractEntityAuthorization<HarvestSeason> {

    public enum Role {
        HARVEST_SEASON_MODERATOR
    }

    public HarvestSeasonAuthorization() {
        allowCRUD(ROLE_ADMIN, Role.HARVEST_SEASON_MODERATOR);
        allow(EntityPermission.READ, ROLE_MODERATOR, ROLE_USER);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final HarvestSeason season,
                                   @Nonnull final UserInfo userInfo) {
        collector.addAuthorizationRole(HarvestSeasonAuthorization.Role.HARVEST_SEASON_MODERATOR,
                () -> userInfo.hasPrivilege(MODERATE_HARVEST_SEASONS));
    }
}
