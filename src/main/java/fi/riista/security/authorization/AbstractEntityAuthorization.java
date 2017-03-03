package fi.riista.security.authorization;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class AbstractEntityAuthorization<T extends BaseEntity> implements EntityAuthorizationStrategy<T> {

    private final Class<T> entityClass;
    private final AuthorizationTokenHelper authorizationTokenHelper;

    @Resource
    private RoleHierarchy roleHierarchy;

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected AbstractEntityAuthorization() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.authorizationTokenHelper = new AuthorizationTokenHelper(entityClass.getSimpleName());
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    protected void allow(final Enum<?> permission, final Enum<?>... tokens) {
        Objects.requireNonNull(permission, "permission is null");
        Objects.requireNonNull(tokens, "tokens is null");
        Stream.of(tokens).forEach(token -> this.authorizationTokenHelper.grant(permission, token));
    }

    protected void allowCRUD(final Enum<?>... tokens) {
        allow(EntityPermission.CREATE, tokens);
        allow(EntityPermission.READ, tokens);
        allow(EntityPermission.UPDATE, tokens);
        allow(EntityPermission.DELETE, tokens);
    }

    @Override
    public boolean hasPermission(@Nonnull final T entity,
                                 @Nonnull final Enum<?> permission,
                                 @Nonnull final Authentication authentication) {
        final AuthorizationTokenCollector tokenCollector = new AuthorizationTokenCollector(
                authentication, roleHierarchy, authorizationTokenHelper, permission);

        if (tokenCollector.hasPermission()) {
            // Short-circuit
            return true;

        } else if (authentication.isAuthenticated()) {
            this.authorizeTarget(tokenCollector, entity, UserInfo.extractFrom(authentication));

            return tokenCollector.hasPermission();

        } else {
            return false;
        }
    }

    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final T target,
                                   @Nonnull final UserInfo userInfo) {
    }
}
