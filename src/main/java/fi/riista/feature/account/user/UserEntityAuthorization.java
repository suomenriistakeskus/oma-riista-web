package fi.riista.feature.account.user;

import fi.riista.feature.account.AccountDTO;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Authorization strategy to make decision on SystemUser domain entity.
 */
@Component
public class UserEntityAuthorization extends AbstractEntityAuthorization {
    public enum Role {
        SELF
    }

    public UserEntityAuthorization() {
        super("SystemUser");

        allow(READ, SystemUser.Role.ROLE_ADMIN, UserEntityAuthorization.Role.SELF);
        allow(CREATE, SystemUser.Role.ROLE_ADMIN);
        allow(UPDATE, SystemUser.Role.ROLE_ADMIN, UserEntityAuthorization.Role.SELF);
        allow(DELETE, SystemUser.Role.ROLE_ADMIN);
    }

    @Override
    protected void authorizeTarget(final AuthorizationTokenCollector collector,
                                   final EntityAuthorizationTarget target,
                                   final UserInfo userInfo) {

        collector.addAuthorizationRole(UserEntityAuthorization.Role.SELF,
                () -> Objects.equals(target.getAuthorizationTargetId(), userInfo.getUserId()));
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class[]{SystemUser.class, SystemUserDTO.class, AccountDTO.class};
    }
}
