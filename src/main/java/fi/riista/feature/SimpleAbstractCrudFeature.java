package fi.riista.feature;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.util.ListTransformer;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public abstract class SimpleAbstractCrudFeature<ID extends Serializable, E extends BaseEntity<ID>, D extends BaseEntityDTO<ID>>
        extends AbstractCrudFeature<ID, E, D> {

    public SimpleAbstractCrudFeature() {
        super();
    }

    public SimpleAbstractCrudFeature(final Class<? extends E> entityClass) {
        super(entityClass);
    }

    protected abstract Function<E, D> entityToDTOFunction();

    @Override
    protected ListTransformer<E, D> dtoTransformer() {
        return new SimpleTransformer();
    }

    private final class SimpleTransformer extends ListTransformer<E, D> {
        @Nonnull
        @Override
        protected List<D> transform(@Nonnull final List<E> list) {
            return list.stream().map(entityToDTOFunction()).collect(toList());
        }
    }
}
