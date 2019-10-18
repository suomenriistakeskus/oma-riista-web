package fi.riista.feature.moderatorarea;

import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;

@Component
public class ModeratorAreaAuthorization extends AbstractEntityAuthorization<ModeratorArea> {

    public ModeratorAreaAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final ModeratorArea area,
                                   @Nonnull final UserInfo userInfo) {
    }
}
