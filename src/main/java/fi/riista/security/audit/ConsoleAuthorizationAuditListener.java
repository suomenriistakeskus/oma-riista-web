package fi.riista.security.audit;

import fi.riista.security.UserInfo;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.spi.AuthorizationAuditListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ConsoleAuthorizationAuditListener implements AuthorizationAuditListener {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationAuditListener.class);

    private final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

    @Override
    public void onAccessDecision(final boolean granted,
                                 final Object permission,
                                 final EntityAuthorizationTarget target,
                                 final Authentication authentication) {
        final String logMessage = createLogMessage(granted, permission, target, authentication);

        if (granted) {
            LOG.info(logMessage);
        } else {
            LOG.warn(logMessage);
        }
    }

    String createLogMessage(final boolean granted,
                            final Object permission,
                            final EntityAuthorizationTarget target,
                            final Authentication authentication) {
        final StringBuilder sb = new StringBuilder();
        buildPermissionPrefix(granted, permission, sb);
        sb.append(' ');
        buildUserIdentifier(authentication, sb);
        sb.append(' ');
        buildTargetIdentifier(target, sb);

        return sb.toString();
    }

    void buildPermissionPrefix(final boolean granted, final Object permission, final StringBuilder sb) {
        sb.append(granted ? "Granted" : "Denied");
        sb.append(" '");
        sb.append(permission);
        sb.append("' permission");
    }

    void buildUserIdentifier(final Authentication authentication, final StringBuilder sb) {
        sb.append("for user ");

        if (authentication == null) {
            sb.append("<missing-authentication>");

        } else if (authenticationTrustResolver.isAnonymous(authentication)) {
            sb.append("<anonymous>");

        } else if (authentication.isAuthenticated()) {
            final UserInfo userInfo = UserInfo.extractFrom(authentication);
            sb.append("[id=");
            sb.append(userInfo.getUserId());
            sb.append(", username='");
            sb.append(userInfo.getUsername());
            sb.append("']");
        } else {
            sb.append("<not-authenticated>");
        }
    }

    void buildTargetIdentifier(final EntityAuthorizationTarget target, final StringBuilder sb) {
        if (target == null) {
            sb.append("for unknown target object");
        } else {
            sb.append("for target object ");
            sb.append("[type='");
            sb.append(target.getAuthorizationTargetName());
            sb.append("', id=");
            sb.append(target.getAuthorizationTargetId());
            sb.append("]");
        }
    }
}
