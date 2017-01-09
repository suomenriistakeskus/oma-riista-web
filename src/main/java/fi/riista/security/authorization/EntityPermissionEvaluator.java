package fi.riista.security.authorization;

import fi.riista.security.authorization.api.AuthorizationTargetFactory;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.spi.AuthorizationAuditListener;
import fi.riista.security.authorization.spi.EntityAuthorizationStrategy;
import fi.riista.security.authorization.support.EntityAuthorizationStrategyRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Objects;

/**
 * Strategy used in expression evaluation to determine whether a user has a permission or permissions
 * for a given domain object.
 * <p/>
 * For usage check @PreAuthorize and supported expressions in
 *
 * @see org.springframework.security.access.expression.SecurityExpressionRoot
 */
@Component
public class EntityPermissionEvaluator implements PermissionEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(EntityPermissionEvaluator.class);

    @Resource
    private EntityAuthorizationStrategyRegistry authorizationStrategyRegistry;

    @Resource
    private AuthorizationTargetFactory authorizationTargetFactory;

    @Resource
    private AuthorizationAuditListener auditListener;

    /**
     * @param authentication     represents the user in question. Should not be null.
     * @param targetDomainObject the domain object for which permissions should be checked. May be null
     *                           in which case implementations should return false, as the null condition can be checked explicitly
     *                           in the expression.
     * @param permission         a representation of the permission object as supplied by the expression system. Not null.
     * @return true if the permission is granted, false otherwise
     */
    @Override
    public boolean hasPermission(final Authentication authentication, final Object targetDomainObject, final Object permission) {
        Objects.requireNonNull(authentication, "User is not authenticated");
        Objects.requireNonNull(permission, "Permission is null");

        final EntityAuthorizationTarget authorizationTarget =
                authorizationTargetFactory.create(targetDomainObject);

        if (authorizationTarget == null) {
            LOG.warn("Unknown authorization target object. permission={} authentication={} target={}",
                    permission, authentication, targetDomainObject);

            return false;
        }

        return hasPermission(authentication, authorizationTarget, permission);
    }

    /**
     * Alternative method for evaluating a permission where only the identifier of the target object
     * is available, rather than the target instance itself.
     *
     * @param authentication represents the user in question. Should not be null.
     * @param targetId       the identifier for the object instance (usually a Long)
     * @param targetType     a String representing the target's type (usually a Java classname). Not null.
     * @param permission     a representation of the permission object as supplied by the expression system. Not null.
     * @return true if the permission is granted, false otherwise
     */
    @Override
    public boolean hasPermission(final Authentication authentication, final Serializable targetId,
                                 final String targetType, final Object permission) {
        Objects.requireNonNull(authentication, "User is not authenticated");
        Objects.requireNonNull(targetType, "Target type is null");
        Objects.requireNonNull(permission, "Permission is null");

        final EntityAuthorizationTarget authorizationTarget =
                authorizationTargetFactory.create(targetType, targetId);

        if (authorizationTarget == null) {
            LOG.warn("Unknown authorization target object. permission={} authentication={} targetType={} targetId={}",
                    permission, authentication, targetType, targetId);

            return false;
        }

        return hasPermission(authentication, authorizationTarget, permission);
    }

    /**
     * Consult matching entity authorization strategy implementation for permission on "target".
     */
    private boolean hasPermission(final Authentication authentication,
                                  final EntityAuthorizationTarget target,
                                  final Object permission) {
        final EntityAuthorizationStrategy authorizationStrategy =
                authorizationStrategyRegistry.lookupAuthorizationStrategy(target);

        final boolean accessGranted = authorizationStrategy.hasPermission(target, permission, authentication);

        // Audit event
        auditListener.onAccessDecision(accessGranted, permission, target, authentication);

        return accessGranted;
    }
}
