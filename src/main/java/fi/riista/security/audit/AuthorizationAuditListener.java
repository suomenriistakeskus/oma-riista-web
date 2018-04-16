package fi.riista.security.audit;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.security.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationAuditListener {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationAuditListener.class);

    private final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

    public void onAccessDecision(final boolean granted,
                                 final Object permission,
                                 final BaseEntity<?> entity,
                                 final Authentication authentication) {
        final String logMessage = createLogMessage(granted, permission, entity, authentication);

        if (granted) {
            LOG.info(logMessage);
        } else {
            LOG.warn(logMessage);
        }
    }

    String createLogMessage(final boolean granted,
                            final Object permission,
                            final BaseEntity<?> entity,
                            final Authentication authentication) {
        final StringBuilder sb = new StringBuilder();
        buildPermissionPrefix(granted, permission, sb);
        sb.append(' ');
        buildUserIdentifier(authentication, sb);
        sb.append(' ');
        buildTargetIdentifier(entity, sb);

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

    void buildTargetIdentifier(final BaseEntity<?> entity, final StringBuilder sb) {
        if (entity == null) {
            sb.append("for unknown target object");
        } else {
            sb.append("for target object ");
            sb.append("[type='");
            sb.append(entity.getClass().getSimpleName());
            sb.append("', id=");
            sb.append(entity.getId());
            sb.append("]");
        }
    }
}
