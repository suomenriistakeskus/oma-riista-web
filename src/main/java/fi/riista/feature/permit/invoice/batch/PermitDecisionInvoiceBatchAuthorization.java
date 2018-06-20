package fi.riista.feature.permit.invoice.batch;

import fi.riista.security.EntityPermission;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import org.springframework.stereotype.Component;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;

@Component
public class PermitDecisionInvoiceBatchAuthorization extends AbstractEntityAuthorization<PermitDecisionInvoiceBatch> {

    public PermitDecisionInvoiceBatchAuthorization() {
        allow(EntityPermission.READ, ROLE_ADMIN, ROLE_MODERATOR);
    }
}
