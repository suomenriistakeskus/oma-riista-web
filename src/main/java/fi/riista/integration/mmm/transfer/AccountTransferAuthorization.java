package fi.riista.integration.mmm.transfer;

import fi.riista.security.EntityPermission;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import org.springframework.stereotype.Component;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;

@Component
public class AccountTransferAuthorization extends AbstractEntityAuthorization<AccountTransfer> {

    public AccountTransferAuthorization() {
        allow(EntityPermission.READ, ROLE_ADMIN, ROLE_MODERATOR);
    }
}
