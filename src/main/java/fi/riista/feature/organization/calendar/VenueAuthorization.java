package fi.riista.feature.organization.calendar;

import fi.riista.feature.organization.rhy.RhyRole;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_USER;

@Component
public class VenueAuthorization extends AbstractEntityAuthorization {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public VenueAuthorization() {
        super("venue");

        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, RhyRole.COORDINATOR, ROLE_USER);

        allow(CREATE, ROLE_ADMIN, ROLE_MODERATOR, RhyRole.COORDINATOR);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, RhyRole.COORDINATOR);
        allow(DELETE, ROLE_ADMIN, ROLE_MODERATOR, RhyRole.COORDINATOR);
    }

    @Override
    protected void authorizeTarget(final AuthorizationTokenCollector collector,
                                   final EntityAuthorizationTarget target,
                                   final UserInfo userInfo) {

        collector.addAuthorizationRole(
                RhyRole.COORDINATOR, () -> userAuthorizationHelper.isCoordinatorAnywhere(userInfo));
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class[] { Venue.class, VenueDTO.class };
    }
}
