package fi.riista.feature.common;

import org.springframework.data.domain.Persistable;

import javax.annotation.Nonnull;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

public class CannotChangeAssociatedEntityException extends RuntimeException {

    public static <T extends Persistable<? extends Serializable>, U extends Persistable<ID>, ID extends Serializable> void assertRelationIdNotChanged(
            @Nonnull final T entity,
            @Nonnull final Function<? super T, U> relationFunction,
            @Nonnull final ID relationId) {

        Objects.requireNonNull(entity, "entity is null");
        Objects.requireNonNull(relationFunction, "relationAttribute is null");
        Objects.requireNonNull(relationId, "relationId is null");

        if (!entity.isNew()) {
            final U relatedObject = relationFunction.apply(entity);

            if (!Objects.equals(relationId, relatedObject.getId())) {
                throw new CannotChangeAssociatedEntityException(String.format(
                        "%s: cannot change ID of related %s: %d -> %d",
                        entity.getClass().getSimpleName(), relatedObject.getClass().getSimpleName(),
                        relatedObject.getId(), relationId));
            }
        }
    }

    public CannotChangeAssociatedEntityException(final String msg) {
        super(msg);
    }

}
