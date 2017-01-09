package fi.riista.security.authorization.support;

import com.google.common.base.MoreObjects;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;

import java.io.Serializable;
import java.util.Objects;

final class AuthorizationTargetImpl<T> implements EntityAuthorizationTarget {
    private final String type;
    private final Serializable id;
    private final Class<?> clazz;
    private final T target;

    AuthorizationTargetImpl(String type, Serializable primaryKey, Class<?> clazz, T targetObject) {
        Objects.requireNonNull(type);

        this.target = targetObject;
        this.id = primaryKey;
        this.clazz = clazz;
        this.type = type;
    }

    @Override
    public String getAuthorizationTargetName() {
        return type;
    }

    @Override
    public Serializable getAuthorizationTargetId() {
        return id;
    }

    @Override
    public boolean is(Class<?> type) {
        return getAuthorizationTarget(type) != null;
    }

    @Override
    public <P> P getAuthorizationTarget(Class<P> type) {
        Objects.requireNonNull(type);

        if (this.target == null) {
            return null;
        }

        if (type.isInstance(target)) {
            return type.cast(target);
        }

        return null;
    }

    @Override
    public Class<?> getAuthorizationTargetClass() {
        return this.clazz;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", type).add("id", id).add("class", clazz).toString();
    }
}
