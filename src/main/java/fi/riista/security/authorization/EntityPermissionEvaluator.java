package fi.riista.security.authorization;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.util.F;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class EntityPermissionEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(EntityPermissionEvaluator.class);

    private final Map<Class, EntityAuthorizationStrategy> registry;

    @Autowired
    public EntityPermissionEvaluator(final List<EntityAuthorizationStrategy> strategies) {
        this.registry = F.index(strategies, EntityAuthorizationStrategy::getEntityClass);
    }

    @SuppressWarnings({"unchecked"})
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasPermission(final Authentication authentication,
                                 final BaseEntity<?> entity,
                                 final Enum<?> permission) {
        Objects.requireNonNull(authentication, "user is null");
        Objects.requireNonNull(entity, "entity is null");
        Objects.requireNonNull(permission, "permission is null");

        return lookupAuthorizationStrategy(entity)
                .map(strategy -> strategy.hasPermission(entity, permission, authentication))
                .orElse(Boolean.FALSE);
    }

    private Optional<EntityAuthorizationStrategy> lookupAuthorizationStrategy(final BaseEntity<?> entity) {
        final Class<?> persistentClass = BaseEntity.getClassWithoutInitializingProxy(entity);

        if (registry.containsKey(persistentClass)) {
            return Optional.of(registry.get(persistentClass));
        }

        LOG.error("No authorization strategy can handle authorizationTarget={}", entity);

        return Optional.empty();
    }
}
