package fi.riista.security.authorization.spi;

import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import org.springframework.security.core.Authentication;

public interface AuthorizationAuditListener {
    void onAccessDecision(
            final boolean granted, final Object permission,
            final EntityAuthorizationTarget target,
            final Authentication authentication);
}
