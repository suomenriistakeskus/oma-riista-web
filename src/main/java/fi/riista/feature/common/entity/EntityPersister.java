package fi.riista.feature.common.entity;

import fi.riista.util.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Persistable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;
import java.util.Collection;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

/**
 * A Spring bean to be used for persisting one or multiple entities in a single
 * Spring-managed transaction.
 */
@Component
public class EntityPersister {

    private static final Logger LOG = LoggerFactory.getLogger(EntityPersister.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveInNewTransaction(@Nonnull final Persistable<?> entity) {
        saveAndFlush(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveInNewTransaction(@Nonnull final Collection<? extends Persistable<?>> entities) {
        saveAndFlush(entities);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public <T extends Persistable<?>> T saveInCurrentlyOpenTransaction(@Nonnull final T entity) {
        return saveAndFlush(entity);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void saveInCurrentlyOpenTransaction(@Nonnull final Collection<? extends Persistable<?>> entities) {
        saveAndFlush(entities);
    }

    private <T extends Persistable<?>> T saveAndFlush(final T entity) {
        save(entity);
        entityManager.flush();
        LOG.debug("Inserted {}(id={}) entity into database.", entity.getClass().getSimpleName(), entity.getId());
        return entity;
    }

    private void saveAndFlush(final Collection<? extends Persistable<?>> entities) {
        Objects.requireNonNull(entities);
        final int numberOfEntities = entities.size();

        if (numberOfEntities > 0) {
            entities.forEach(this::save);
            entityManager.flush();
            LOG.debug("Inserted {} entities into database.", numberOfEntities);
        }
    }

    private void save(final Persistable<?> entity) {
        try {
            Objects.requireNonNull(entity);
            entityManager.persist(entity);
        } catch (final ConstraintViolationException cve) {
            throw new ConstraintViolationException(
                    cve.getConstraintViolations().stream()
                            .map(Functions.CONSTRAINT_VIOLATION_TO_STRING)
                            .collect(joining("\n", "Violated JSR-303 constraints:\n", "")),
                    cve.getConstraintViolations());
        }
    }

}
