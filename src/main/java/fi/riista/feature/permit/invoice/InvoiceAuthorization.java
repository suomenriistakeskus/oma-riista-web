package fi.riista.feature.permit.invoice;

import fi.riista.security.EntityPermission;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import org.springframework.stereotype.Component;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;

@Component
public class InvoiceAuthorization extends AbstractEntityAuthorization<Invoice> {

    public InvoiceAuthorization() {
        allow(EntityPermission.READ, ROLE_ADMIN, ROLE_MODERATOR);
        allow(EntityPermission.UPDATE, ROLE_ADMIN, ROLE_MODERATOR);
    }
}
