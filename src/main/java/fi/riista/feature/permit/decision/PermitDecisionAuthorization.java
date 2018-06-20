package fi.riista.feature.permit.decision;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class PermitDecisionAuthorization extends AbstractEntityAuthorization<PermitDecision> {

    public PermitDecisionAuthorization() {
        allow(EntityPermission.READ, SystemUser.Role.ROLE_MODERATOR, SystemUser.Role.ROLE_ADMIN);
        allow(EntityPermission.UPDATE, SystemUser.Role.ROLE_MODERATOR, SystemUser.Role.ROLE_ADMIN);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final PermitDecision decision,
                                   @Nonnull final UserInfo userInfo) {
    }
}
