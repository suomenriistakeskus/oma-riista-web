package fi.riista.feature.common.decision.authority.rka;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class DecisionRkaAuthorityAuthorization extends AbstractEntityAuthorization<DecisionRkaAuthority> {

    public DecisionRkaAuthorityAuthorization() {
        allowCRUD(SystemUser.Role.ROLE_MODERATOR, SystemUser.Role.ROLE_ADMIN);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final DecisionRkaAuthority authority,
                                   @Nonnull final UserInfo userInfo) {
    }

}
