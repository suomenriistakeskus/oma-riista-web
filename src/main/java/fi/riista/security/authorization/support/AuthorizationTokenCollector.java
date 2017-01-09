package fi.riista.security.authorization.support;

import fi.riista.security.authorization.ConditionalAuthorization;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class AuthorizationTokenCollector {
    private final Set<String> acquiredTokens = new HashSet<>();
    private final AuthorizationTokenHelper authorizationTokenHelper;
    private final Object permission;

    public AuthorizationTokenCollector(final Authentication authentication,
                                       final RoleHierarchy roleHierarchy,
                                       final AuthorizationTokenHelper authorizationTokenHelper,
                                       final Object permission) {
        this.authorizationTokenHelper = authorizationTokenHelper;

        // Initialize default role authorization tokens using active user role and role hierarchy
        this.acquiredTokens.addAll(AuthorityUtils.authorityListToSet(
                roleHierarchy.getReachableGrantedAuthorities(authentication.getAuthorities())));

        this.permission = permission;
    }

    public <E extends Enum<E>> void addAuthorizationRole(final E authorizationRole) {
        Objects.requireNonNull(authorizationRole, "authorizationRole == null");

        this.acquiredTokens.add(AuthorizationTokenHelper.getCanonicalAuthorizationToken(
                Objects.requireNonNull(authorizationRole, "authorizationRole is null")));
    }

    public <E extends Enum<E>> void addAuthorizationRole(final E authorizationRole,
                                                         final ConditionalAuthorization condition) {
        // Skip condition evaluation if already granted
        if (hasPermission()) {
            return;
        }

        if (authorizationTokenHelper.canAcceptRoleForPermission(permission, authorizationRole)) {
            if (condition.applies()) {
                addAuthorizationRole(authorizationRole);
            }
        }
    }

    public boolean hasPermission() {
        return authorizationTokenHelper.hasPermission(permission, acquiredTokens);
    }
}
