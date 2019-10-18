package fi.riista.feature.permit.invoice;

import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.account.user.SystemUserPrivilege.ALTER_INVOICE_PAYMENT;
import static fi.riista.feature.permit.invoice.InvoiceAuthorization.Role.INVOICE_PAYMENT_MODERATOR;

@Component
public class InvoiceAuthorization extends AbstractEntityAuthorization<Invoice> {

    public enum InvoicePermission {
        CREATE_REMOVE_PAYMENT_LINES_MANUALLY
    }

    enum Role {
        INVOICE_PAYMENT_MODERATOR
    }

    public InvoiceAuthorization() {
        allow(EntityPermission.READ, ROLE_ADMIN, ROLE_MODERATOR);
        allow(EntityPermission.UPDATE, ROLE_ADMIN, ROLE_MODERATOR);
        allow(InvoicePermission.CREATE_REMOVE_PAYMENT_LINES_MANUALLY, ROLE_ADMIN, INVOICE_PAYMENT_MODERATOR);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final Invoice invoice,
                                   @Nonnull final UserInfo userInfo) {

        collector.addAuthorizationRole(INVOICE_PAYMENT_MODERATOR, () -> {
            return userInfo.isModerator() && userInfo.hasPrivilege(ALTER_INVOICE_PAYMENT);
        });
    }
}
