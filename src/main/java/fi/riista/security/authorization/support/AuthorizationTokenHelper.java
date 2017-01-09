package fi.riista.security.authorization.support;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class AuthorizationTokenHelper {
    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationTokenHelper.class);

    private static final String DEFAULT_ROLE_PREFIX = "ROLE_";

    public static String getCanonicalAuthorizationToken(Enum<?> authorizationRole) {
        return authorizationRole.name().startsWith(DEFAULT_ROLE_PREFIX)
                ? authorizationRole.name()
                : String.format("%s.%s",
                        authorizationRole.getDeclaringClass().getSimpleName(),
                        authorizationRole.name());
    }

    public static String getPermissionName(final Object permission) {
        return permission.toString().toLowerCase();
    }

    private final Map<String, Set<String>> authorizationData = new HashMap<>();
    private final String targetName;

    public AuthorizationTokenHelper(String name) {
        this.targetName = name;
    }

    private Set<String> getGrantedRolesForPermission(final String permission) {
        final Set<String> permissionSet = authorizationData.get(permission);
        if (permissionSet == null) {
            return Collections.emptySet();
        }
        return permissionSet;
    }

    public <E extends Enum<E>> boolean canAcceptRoleForPermission(final Object permission, final E authorizationRole) {
        final String permissionName = getPermissionName(permission);
        final String tokenName = getCanonicalAuthorizationToken(authorizationRole);
        final Set<String> permissionConfiguration = authorizationData.get(permissionName);

        return permissionConfiguration != null && permissionConfiguration.contains(tokenName);
    }

    public boolean hasPermission(final Object permission, final Set<String> acquiredTokens) {
        final Set<String> permissionSet = getGrantedRolesForPermission(getPermissionName(permission));

        LOG.debug("Checking permission {} set {} for acquiredTokens {}",
                permission, StringUtils.collectionToCommaDelimitedString(permissionSet),
                StringUtils.collectionToCommaDelimitedString(acquiredTokens));

        return Sets.intersection(permissionSet, acquiredTokens).size() > 0;
    }

    public void grant(final Object permission, final Enum<?> token) {
        final String permissionName = getPermissionName(permission);
        final String tokenName = getCanonicalAuthorizationToken(token);
        final Set<String> permissionConfiguration = authorizationData.get(permissionName);

        LOG.debug("Granting '{}' permission '{}' for token [{}]",
                this.targetName, permissionName, tokenName);

        if (permissionConfiguration == null) {
            HashSet<String> set = new HashSet<>();
            set.add(tokenName);

            authorizationData.put(permissionName, set);
        } else {
            permissionConfiguration.add(tokenName);
        }
    }
}
