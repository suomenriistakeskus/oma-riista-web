package fi.riista.security.authorization;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class AuthorizationTokenHelper {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationTokenHelper.class);
    private static final String DEFAULT_ROLE_PREFIX = "ROLE_";

    private final Map<String, Set<String>> authorizationData = new HashMap<>();
    private final String targetName;

    AuthorizationTokenHelper(final String name) {
        this.targetName = name;
    }

    private static String canonicalEnumName(final Enum<?> enumValue) {
        return String.format("%s.%s", enumValue.getDeclaringClass().getCanonicalName(), enumValue.name());
    }

    static String getAuthorizationRoleName(final Enum<?> authorizationRole) {
        if (authorizationRole.name().startsWith(DEFAULT_ROLE_PREFIX)) {
            return authorizationRole.name();
        }
        return canonicalEnumName(authorizationRole);
    }

    private static String getPermissionName(final Enum<?> permission) {
        return canonicalEnumName(permission);
    }

    private Set<String> getGrantedRolesForPermission(final String permission) {
        final Set<String> permissionSet = authorizationData.get(permission);
        return permissionSet == null ? Collections.emptySet() : permissionSet;
    }

    boolean canAcceptRoleForPermission(final Enum<?> permission, final Enum<?> authorizationRole) {
        final String permissionName = getPermissionName(permission);
        final Set<String> permissionConfiguration = authorizationData.get(permissionName);

        return permissionConfiguration != null &&
                permissionConfiguration.contains(getAuthorizationRoleName(authorizationRole));
    }

    public boolean hasPermission(final Enum<?> permission, final Set<String> acquiredTokens) {
        final Set<String> permissionSet = getGrantedRolesForPermission(getPermissionName(permission));

        LOG.debug("Checking permission {} set {} for acquiredTokens {}",
                permission, StringUtils.collectionToCommaDelimitedString(permissionSet),
                StringUtils.collectionToCommaDelimitedString(acquiredTokens));

        return Sets.intersection(permissionSet, acquiredTokens).size() > 0;
    }

    public void grant(final Enum<?> permission, final Enum<?> token) {
        final String permissionName = getPermissionName(permission);
        final String tokenName = getAuthorizationRoleName(token);

        LOG.debug("Granting '{}' permission '{}' for token [{}]", this.targetName, permissionName, tokenName);

        authorizationData.computeIfAbsent(permissionName, key -> new HashSet<>()).add(tokenName);
    }
}
