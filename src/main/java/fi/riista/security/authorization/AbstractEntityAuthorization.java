package fi.riista.security.authorization;

import fi.riista.security.UserInfo;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.spi.EntityAuthorizationStrategy;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import fi.riista.security.authorization.support.AuthorizationTokenHelper;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class AbstractEntityAuthorization implements EntityAuthorizationStrategy {

    @Resource
    private RoleHierarchy roleHierarchy;

    /**
     * Configuration helper stores mappings from
     * permission names to granted entity role token
     */
    private final AuthorizationTokenHelper authorizationTokenHelper;

    private final String supportedEntityName;

    protected AbstractEntityAuthorization(String name) {
        this.authorizationTokenHelper = new AuthorizationTokenHelper(name);
        this.supportedEntityName = name;
    }

    protected void allow(final Object permission, final Enum<?> token) {
        Objects.requireNonNull(permission, "permission is null");
        Objects.requireNonNull(token, "token is null");

        this.authorizationTokenHelper.grant(permission, token);
    }

    protected void allow(final Object permission, final Collection<Enum<?>> tokens) {
        Objects.requireNonNull(tokens, "tokens is null");
        tokens.forEach(token -> allow(permission, token));
    }

    protected void allow(final Object permission, final Enum<?>... tokens) {
        Objects.requireNonNull(tokens, "tokens is null");
        Stream.of(tokens).forEach(token -> allow(permission, token));
    }

    @Override
    public String getEntityName() {
        return supportedEntityName;
    }

    @Override
    public abstract Class<?>[] getSupportedTypes();

    @Override
    public boolean hasPermission(final EntityAuthorizationTarget target,
                                 final Object permission,
                                 final Authentication authentication) {
        final AuthorizationTokenCollector tokenCollector = createCollectorForPermission(authentication, permission);

        if (tokenCollector.hasPermission()) {
            // Short-circuit
            return true;

        } else if (authentication.isAuthenticated()) {
            this.authorizeTarget(tokenCollector, target, UserInfo.extractFrom(authentication));

            return tokenCollector.hasPermission();

        } else {
            return false;
        }
    }

    protected AuthorizationTokenCollector createCollectorForPermission(final Authentication authentication,
                                                                       final Object permission) {
        return new AuthorizationTokenCollector(authentication, roleHierarchy, authorizationTokenHelper, permission);
    }

    /**
     * Enumerate any additional roles which apply to relationship
     * with authenticated user and supported entity instance.
     */
    protected abstract void authorizeTarget(final AuthorizationTokenCollector collector,
                                            final EntityAuthorizationTarget target,
                                            final UserInfo userInfo);
}
