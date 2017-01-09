package fi.riista.security.authorization;

import static fi.riista.util.ClassUtils.getTypeArgumentOfSuperClass;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.annotation.Nonnull;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public abstract class SimpleEntityDTOAuthorization<ENTITY extends BaseEntity<ID>, DTO extends BaseEntityDTO<ID>, ID extends Serializable>
        extends AbstractEntityAuthorization {

    public final Class<ENTITY> entityClass;
    public final Class<DTO> dtoClass;

    @SuppressWarnings("unchecked")
    public SimpleEntityDTOAuthorization(final String name) {
        super(name);

        this.entityClass = (Class<ENTITY>) getTypeArgumentOfSuperClass(this, SimpleEntityDTOAuthorization.class, 0);
        this.dtoClass = (Class<DTO>) getTypeArgumentOfSuperClass(this, SimpleEntityDTOAuthorization.class, 1);
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class<?>[] { entityClass, dtoClass };
    }

    protected abstract JpaRepository<ENTITY, ID> getRepository();

    protected Optional<ENTITY> findEntity(@Nonnull final EntityAuthorizationTarget target) {
        Objects.requireNonNull(target);
        final ENTITY entity = target.getAuthorizationTarget(entityClass);
        return entity != null ? Optional.of(entity) : findAuthorizationTargetId(target).map(getRepository()::findOne);
    }

    protected Optional<DTO> findDto(@Nonnull final EntityAuthorizationTarget target) {
        Objects.requireNonNull(target);
        return Optional.ofNullable(target.getAuthorizationTarget(dtoClass));
    }

    @SuppressWarnings("unchecked")
    private Optional<ID> findAuthorizationTargetId(final EntityAuthorizationTarget target) {
        return Optional.ofNullable(target.getAuthorizationTargetId()).map(serializableId -> (ID) serializableId);
    }

}
