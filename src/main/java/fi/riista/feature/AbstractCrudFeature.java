package fi.riista.feature;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.error.NotFoundException;
import fi.riista.security.EntityPermission;
import fi.riista.util.DtoUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;

public abstract class AbstractCrudFeature<ID extends Serializable, E extends BaseEntity<ID>, D extends BaseEntityDTO<ID>> {

    @Resource
    protected ActiveUserService activeUserService;

    private Class<? extends E> entityClass;

    @SuppressWarnings("unchecked")
    protected AbstractCrudFeature() {
        this.entityClass =
                (Class<? extends E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    }

    protected AbstractCrudFeature(final Class<? extends E> entityClass) {
        this.entityClass = Objects.requireNonNull(entityClass);
    }

    protected abstract JpaRepository<E, ID> getRepository();

    protected abstract void updateEntity(E entity, D dto);

    protected abstract D toDTO(@Nonnull E entity);

    @Transactional(readOnly = true)
    public D read(ID id) {
        return toDTO(requireEntity(id, EntityPermission.READ));
    }

    @Transactional
    public D create(D dto) {
        final E entity = createEntity();

        updateEntity(entity, dto);

        // Permission check
        activeUserService.assertHasPermission(entity, getCreatePermission(entity, dto));

        final E persisted = getRepository().saveAndFlush(entity);
        afterCreate(persisted, dto);
        return toDTO(persisted);
    }

    protected void afterCreate(E entity, D dto) {
    }

    protected void afterUpdate(E entity, D dto) {
    }

    protected Enum<?> getCreatePermission(@SuppressWarnings("unused") E entity, @SuppressWarnings("unused") D dto) {
        return EntityPermission.CREATE;
    }

    private E createEntity() {
        try {
            return entityClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Could not create entity", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could access constructor", e);
        }
    }

    @Transactional
    public D update(D dto) {
        final E entity = requireEntity(dto.getId());

        activeUserService.assertHasPermission(entity, getUpdatePermission(entity, dto));

        checkForUpdateConflict(dto, entity);

        updateEntity(entity, dto);

        // Must use saveAndFlush() to update returned consistencyVersion == dto.revision
        final E persisted = getRepository().saveAndFlush(entity);
        afterUpdate(persisted, dto);
        return toDTO(persisted);
    }

    protected Enum<?> getUpdatePermission(@SuppressWarnings("unused") E entity, @SuppressWarnings("unused") D dto) {
        return EntityPermission.UPDATE;
    }

    @Transactional
    public void delete(ID id) {
        final E entity = requireEntity(id);
        activeUserService.assertHasPermission(entity, getDeletePermission(entity));
        delete(entity);
    }

    protected void delete(E entity) {
        getRepository().delete(entity);
    }

    protected Enum<?> getDeletePermission(@SuppressWarnings("unused") E entity) {
        return EntityPermission.DELETE;
    }

    protected E requireEntity(final ID id) {
        Objects.requireNonNull(id, "Entity primary key is required");

        return getRepository().findById(id)
                .orElseThrow(() -> new NotFoundException("No such " + entityClass.getCanonicalName() + " id=" + id));
    }

    protected E requireEntity(final ID id, Enum<?> permission) {
        final E entity = requireEntity(id);

        // Permission check
        activeUserService.assertHasPermission(entity, permission);

        return entity;
    }

    protected void checkForUpdateConflict(D dto, E entity) {
        DtoUtil.assertNoVersionConflict(entity, dto);
    }
}
