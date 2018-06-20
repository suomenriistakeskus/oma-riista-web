package fi.riista.security.authorization;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class AuthorizationTokenCollector {

    private final Set<String> acquiredTokens = new HashSet<>();
    private final AuthorizationTokenHelper authorizationTokenHelper;
    private final Enum<?> permission;

    public AuthorizationTokenCollector(final Authentication authentication,
                                       final RoleHierarchy roleHierarchy,
                                       final AuthorizationTokenHelper authorizationTokenHelper,
                                       final Enum<?> permission) {

        this.authorizationTokenHelper = Objects.requireNonNull(authorizationTokenHelper);

        // Initialize default role authorization tokens using active user role and role hierarchy
        if (authentication != null && authentication.isAuthenticated()) {
            this.acquiredTokens.addAll(AuthorityUtils.authorityListToSet(
                    roleHierarchy.getReachableGrantedAuthorities(authentication.getAuthorities())));
        }

        this.permission = Objects.requireNonNull(permission);
    }

    public void addAuthorizationRole(final Enum<?> authorizationRole) {
        Objects.requireNonNull(authorizationRole, "authorizationRole is null");

        this.acquiredTokens.add(AuthorizationTokenHelper.getAuthorizationRoleName(
                Objects.requireNonNull(authorizationRole, "authorizationRole is null")));
    }

    public void addAuthorizationRole(final Enum<?> authorizationRole, final ConditionalAuthorization condition) {
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
