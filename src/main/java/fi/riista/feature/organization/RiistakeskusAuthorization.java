package fi.riista.feature.organization;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;

@Component
public class RiistakeskusAuthorization extends AbstractEntityAuthorization<Riistakeskus> {

    public enum Permission {
        LIST_ANNUAL_STATISTICS
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public RiistakeskusAuthorization() {
        allow(EntityPermission.READ, ROLE_ADMIN, ROLE_MODERATOR);
        allow(EntityPermission.UPDATE, ROLE_ADMIN, ROLE_MODERATOR);
        allow(Permission.LIST_ANNUAL_STATISTICS, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final Riistakeskus rk,
                                   @Nonnull final UserInfo userInfo) {

        collector.addAuthorizationRole(TOIMINNANOHJAAJA, () -> {
            return userAuthorizationHelper.isCoordinatorAnywhere(userInfo);
        });
    }
}
