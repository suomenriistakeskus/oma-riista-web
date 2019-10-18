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
import static fi.riista.feature.account.user.SystemUserPrivilege.MODERATE_RHY_ANNUAL_STATISTICS;
import static fi.riista.feature.organization.RiistakeskusAuthorization.Role.RHY_ANNUAL_STATISTICS_MODERATOR;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;

@Component
public class RiistakeskusAuthorization extends AbstractEntityAuthorization<Riistakeskus> {

    public enum Permission {
        LIST_ANNUAL_STATISTICS,
        BATCH_APPROVE_ANNUAL_STATISTICS
    }

    enum Role {
        RHY_ANNUAL_STATISTICS_MODERATOR
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public RiistakeskusAuthorization() {
        allow(EntityPermission.READ, ROLE_ADMIN, ROLE_MODERATOR);
        allow(EntityPermission.UPDATE, ROLE_ADMIN, ROLE_MODERATOR);
        allow(Permission.LIST_ANNUAL_STATISTICS, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(Permission.BATCH_APPROVE_ANNUAL_STATISTICS, ROLE_ADMIN, RHY_ANNUAL_STATISTICS_MODERATOR);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final Riistakeskus rk,
                                   @Nonnull final UserInfo userInfo) {

        if (userInfo.isModerator()) {
            collector.addAuthorizationRole(RHY_ANNUAL_STATISTICS_MODERATOR, () -> {
                return userInfo.hasPrivilege(MODERATE_RHY_ANNUAL_STATISTICS);
            });

        } else {
            collector.addAuthorizationRole(TOIMINNANOHJAAJA, () -> {
                return userAuthorizationHelper.isCoordinatorAnywhere(userInfo);
            });
        }
    }
}
