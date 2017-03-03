package fi.riista.feature.organization.calendar;

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
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_USER;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;

@Component
public class VenueAuthorization extends AbstractEntityAuthorization<Venue> {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public VenueAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR);
        allowCRUD(TOIMINNANOHJAAJA);

        allow(EntityPermission.READ, ROLE_USER);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final Venue venue,
                                   @Nonnull final UserInfo userInfo) {
        collector.addAuthorizationRole(TOIMINNANOHJAAJA, () -> userAuthorizationHelper.isCoordinatorAnywhere(userInfo));
    }
}
