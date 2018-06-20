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
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

@Component
public class RhyAnnualStatisticsAuthorization extends AbstractEntityAuthorization<RhyAnnualStatistics> {

    public enum Permission {
        APPROVE
    }

    @Resource
    private RhyAuthorizationHelper helper;

    public RhyAnnualStatisticsAuthorization() {
        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(CREATE, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(Permission.APPROVE, ROLE_ADMIN, ROLE_MODERATOR);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final RhyAnnualStatistics statistics,
                                   @Nonnull final UserInfo userInfo) {

        helper.collectAllRhyRoles(statistics.getRhy(), collector, userInfo);
    }
}
