package fi.riista.feature;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.error.NotFoundException;
import fi.riista.security.EntityPermission;
import fi.riista.util.DtoUtil;
import fi.riista.util.ListTransformer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @param <ID> Entity's id class, often Long.class
 * @param <E>  Entity's class
 * @param <D>  DTO class corresponding to the entity
 */
public abstract class AbstractCrudFeature<ID extends Serializable, E extends BaseEntity<ID>, D extends BaseEntityDTO<ID>> {

    @Resource
    protected ActiveUserService activeUserService;

    private Class<? extends E> entityClass;

    protected AbstractCrudFeature(Class<? extends E> entityClass) {
        this.entityClass = entityClass;
    }

    @SuppressWarnings("unchecked")
    protected AbstractCrudFeature() {
        this.entityClass =
                (Class<? extends E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    }

    protected abstract JpaRepository<E, ID> getRepository();

    protected abstract void updateEntity(E entity, D dto);

    protected abstract ListTransformer<E, D> dtoTransformer();

    @Transactional(readOnly = true)
    public D read(ID id) {
        return dtoTransformer().apply(requireEntity(id, EntityPermission.READ));
    }

    @Transactional
    public D create(D dto) {
        // Permission check
        assertHasCreatePermission(dto);

        final E entity = createEntity();

        updateEntity(entity, dto);

        return dtoTransformer().apply(getRepository().saveAndFlush(entity));
    }

    // Subclasses can override this to use some other instance type in create permission check.
    protected void assertHasCreatePermission(final D dto) {
        activeUserService.assertHasPermission(dto, getCreatePermission(dto));
    }

    protected Enum<?> getCreatePermission(@SuppressWarnings("unused") D dto) {
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
        return dtoTransformer().apply(getRepository().saveAndFlush(entity));
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

        return Optional.ofNullable(getRepository().findOne(id))
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

    // Page of entities
    protected Page<D> toDTO(final Page<E> resultPage, final Pageable pageRequest) {
        Objects.requireNonNull(resultPage, "resultPage must not be null");
        Objects.requireNonNull(pageRequest, "pageRequest must not be null");

        final List<D> transformedList = dtoTransformer().apply(resultPage.getContent());
        final List<D> resultList = transformedList != null ? transformedList : Collections.emptyList();

        return new PageImpl<>(resultList, pageRequest, resultPage.getTotalElements());
    }
}
