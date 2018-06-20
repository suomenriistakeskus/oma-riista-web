package fi.riista.feature.organization;

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
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

@Component
public class OrganisationAuthorization extends AbstractEntityAuthorization<Organisation> {

    @Resource
    private RhyAuthorizationHelper helper;

    public OrganisationAuthorization() {
        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final Organisation organisation,
                                   @Nonnull final UserInfo userInfo) {

        helper.collectAllRhyRoles(organisation, collector, userInfo);
    }
}
