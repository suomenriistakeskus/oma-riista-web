package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.rhy.RhyAuthorizationHelper;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.account.user.SystemUserPrivilege.MODERATE_RHY_ANNUAL_STATISTICS;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsAuthorization.Role.RHY_ANNUAL_STATISTICS_MODERATOR;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

@Component
public class RhyAnnualStatisticsAuthorization extends AbstractEntityAuthorization<RhyAnnualStatistics> {

    public enum Permission {

        // Updating fields allowed only for moderator
        MODERATOR_UPDATE,

        CHANGE_APPROVAL_STATUS
    }

    enum Role {
        RHY_ANNUAL_STATISTICS_MODERATOR
    }

    @Resource
    private RhyAuthorizationHelper helper;

    public RhyAnnualStatisticsAuthorization() {
        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(CREATE, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(UPDATE, ROLE_ADMIN, RHY_ANNUAL_STATISTICS_MODERATOR, TOIMINNANOHJAAJA);
        allow(Permission.MODERATOR_UPDATE, ROLE_ADMIN, RHY_ANNUAL_STATISTICS_MODERATOR);
        allow(Permission.CHANGE_APPROVAL_STATUS, ROLE_ADMIN, RHY_ANNUAL_STATISTICS_MODERATOR);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final RhyAnnualStatistics statistics,
                                   @Nonnull final UserInfo userInfo) {

        if (userInfo.isModerator()) {
            collector.addAuthorizationRole(RHY_ANNUAL_STATISTICS_MODERATOR, () -> {
                return userInfo.hasPrivilege(MODERATE_RHY_ANNUAL_STATISTICS);
            });

        } else {
            helper.collectAllRhyRoles(statistics.getRhy(), collector, userInfo);
        }
    }
}
