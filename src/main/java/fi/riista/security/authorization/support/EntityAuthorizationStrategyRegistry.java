package fi.riista.security.authorization.support;

import com.google.common.collect.Maps;

import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.spi.EntityAuthorizationStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class EntityAuthorizationStrategyRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(EntityAuthorizationStrategyRegistry.class);

    @Resource
    private List<EntityAuthorizationStrategy> strategies;

    private final Map<Class<?>, EntityAuthorizationStrategy> byEntityClassAuthorizations = Maps.newHashMap();
    private final Map<String, EntityAuthorizationStrategy> byEntityNameAuthorizations = Maps.newHashMap();

    /**
     * Initialize lookup arrays from entity authorization strategies
     */
    @PostConstruct
    protected void configure() {
        for (final EntityAuthorizationStrategy authorizationStrategy : strategies) {
            LOG.debug("Registered authorization strategy for target entity {}", authorizationStrategy.getEntityName());

            byEntityNameAuthorizations.put(authorizationStrategy.getEntityName(), authorizationStrategy);
            for (final Class<?> clazz : authorizationStrategy.getSupportedTypes()) {
                LOG.debug("Registered authorization strategy for target class {}", clazz.getCanonicalName());

                if (clazz.getCanonicalName() != null) {
                    byEntityNameAuthorizations.put(clazz.getCanonicalName(), authorizationStrategy);
                }

                byEntityClassAuthorizations.put(clazz, authorizationStrategy);
            }
        }
    }

    public EntityAuthorizationStrategy lookupAuthorizationStrategy(final EntityAuthorizationTarget target) {
        if (target != null) {
            if (target.getAuthorizationTargetClass() != null) {
                final Class<?> targetClass = target.getAuthorizationTargetClass();
                final EntityAuthorizationStrategy strategy = lookupByTargetClass(targetClass);

                if (strategy != null) {
                    return strategy;
                }
            }

            if (target.getAuthorizationTargetName() != null) {
                final EntityAuthorizationStrategy strategy = lookupByTargetName(target);

                if (strategy != null) {
                    return strategy;
                }
            }
        }

        LOG.error("No authorization strategy can handle authorizationTarget={}", target);

        return DENY_ALL_INSTANCE;
    }

    private EntityAuthorizationStrategy lookupByTargetName(final EntityAuthorizationTarget target) {
        return byEntityNameAuthorizations.get(target.getAuthorizationTargetName());
    }

    private EntityAuthorizationStrategy lookupByTargetClass(final Class<?> targetClass) {
        if (byEntityClassAuthorizations.containsKey(targetClass)) {
            return byEntityClassAuthorizations.get(targetClass);
        }

        final String canonicalName = targetClass.getCanonicalName();
        if (byEntityNameAuthorizations.containsKey(canonicalName)) {
            return byEntityNameAuthorizations.get(canonicalName);
        }

        return null;
    }

    private static final EntityAuthorizationStrategy DENY_ALL_INSTANCE = new DenyAllEntityAuthorizationStrategy();

    private static class DenyAllEntityAuthorizationStrategy implements EntityAuthorizationStrategy {
        @Override
        public String getEntityName() {
            return "notImplemented";
        }

        @Override
        public Class<?>[] getSupportedTypes() {
            return new Class[] { Void.class };
        }

        @Override
        public boolean hasPermission(EntityAuthorizationTarget authorizationTarget, Object permission, Authentication authentication) {
            return false;
        }
    }
}
