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
import static fi.riista.feature.organization.occupation.OccupationType.SRVA_YHTEYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;

@Component
public class OrganisationAuthorization extends AbstractEntityAuthorization<Organisation> {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public OrganisationAuthorization() {
        allow(EntityPermission.READ, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(EntityPermission.UPDATE, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final Organisation organisation,
                                   @Nonnull final UserInfo userInfo) {
        if (organisation.getOrganisationType() == OrganisationType.RHY) {
            collector.addAuthorizationRole(TOIMINNANOHJAAJA, () ->
                    userAuthorizationHelper.isCoordinator(organisation, userInfo));

            collector.addAuthorizationRole(SRVA_YHTEYSHENKILO, () ->
                    userAuthorizationHelper.isSrvaContactPerson(organisation, userInfo));
        }
    }
}
