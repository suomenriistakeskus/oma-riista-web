package fi.riista.security.authorization.support;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.security.authorization.api.AuthorizationTargetFactory;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;

import org.apache.commons.lang.reflect.MethodUtils;
import org.hibernate.proxy.HibernateProxyHelper;
import org.springframework.data.domain.Persistable;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Objects;

@Component
public class AuthorizationTargetFactoryImpl implements AuthorizationTargetFactory {

    @Override
    public EntityAuthorizationTarget create(final Object targetDomainObject) {
        if (targetDomainObject == null) {
            return null;

        } else if (targetDomainObject instanceof Class<?>) {
            return forClass((Class<?>) targetDomainObject);

        } else if (targetDomainObject instanceof String) {
            return decodeFromString((String) targetDomainObject);

        } else if (targetDomainObject instanceof EntityAuthorizationTarget) {
            return (EntityAuthorizationTarget) targetDomainObject;

        } else if (targetDomainObject instanceof Persistable) {
            return forPersistentEntity((Persistable<?>) targetDomainObject);

        } else if (targetDomainObject instanceof BaseEntityDTO) {
            return forDTO((BaseEntityDTO<?>) targetDomainObject);

        } else {
            return forObject(targetDomainObject);
        }
    }

    @Override
    public AuthorizationTargetImpl<?> create(String type, Serializable primaryKey) {
        return new AuthorizationTargetImpl<>(type, primaryKey, null, null);
    }

    static AuthorizationTargetImpl<Persistable<?>> forPersistentEntity(Persistable<?> persistentEntity) {
        Objects.requireNonNull(persistentEntity);

        final Class<?> typeClass = HibernateProxyHelper.getClassWithoutInitializingProxy(persistentEntity);
        final String type = getTypeNameFor(typeClass);

        return new AuthorizationTargetImpl<>(type, persistentEntity.getId(), typeClass, persistentEntity);
    }

    static AuthorizationTargetImpl<BaseEntityDTO<?>> forDTO(BaseEntityDTO<?> dto) {
        Objects.requireNonNull(dto);

        final String type = getTypeNameFor(dto.getClass());

        return new AuthorizationTargetImpl<>(type, dto.getId(), dto.getClass(), dto);
    }

    static AuthorizationTargetImpl<Class<?>> forClass(final Class<?> targetClass) {
        return new AuthorizationTargetImpl<>(targetClass.getSimpleName(), null, targetClass, null);
    }

    static AuthorizationTargetImpl<?> forObject(Object object) {
        Objects.requireNonNull(object);

        final String type = getTypeNameFor(object.getClass());
        final Serializable primaryKey = getPrimaryKeyForInstance(object);

        return new AuthorizationTargetImpl<>(type, primaryKey, object.getClass(), object);
    }

    static AuthorizationTargetImpl<?> decodeFromString(final String stringEntityReference) {
        final String[] parts = String.class.cast(stringEntityReference).split(":");

        final String targetType = parts.length > 0 ? parts[0].trim() : null;
        final Long targetId = parts.length > 1 ? Long.valueOf(parts[1]) : null;

        if (targetType == null || targetType.isEmpty()) {
            return null;
        }

        return new AuthorizationTargetImpl<>(targetType, targetId, null, null);
    }

    static String getTypeNameFor(Class<?> typeClass) {
        return typeClass.getCanonicalName();
    }

    static Serializable getPrimaryKeyForInstance(Object instance) {
        final Class<?> typeClass = HibernateProxyHelper.getClassWithoutInitializingProxy(instance);

        final Method method = MethodUtils.getAccessibleMethod(typeClass, "getId", new Class[] {});

        if (method == null) {
            return null;
        }

        final Object primaryKey;
        try {
            primaryKey = method.invoke(instance);

            if (primaryKey instanceof Serializable) {
                return (Serializable) primaryKey;
            }
            throw new IllegalArgumentException("Primary key should be serializable");
        } catch (Exception e) {
            throw new RuntimeException("Could not execute entity getId() method", e);
        }
    }

}
