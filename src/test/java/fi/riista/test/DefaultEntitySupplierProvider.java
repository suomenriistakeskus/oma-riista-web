package fi.riista.test;

import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.organization.Riistakeskus;
import fi.riista.util.NumberSequence;
import org.springframework.data.domain.Persistable;

import java.util.AbstractList;
import java.util.List;

public interface DefaultEntitySupplierProvider extends EntitySupplierProvider {

    static final List<Persistable<?>> EMPTY_PERSISTABLE_LIST = new AbstractList<Persistable<?>>() {
        @Override
        public Persistable<?> get(final int index) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void add(final int index, final Persistable<?> object) {
            // Do nothing.
        }
    };

    @Override
    default EntitySupplier getEntitySupplier() {
        return new EntitySupplier(NumberSequence.INSTANCE, EMPTY_PERSISTABLE_LIST, () -> new Riistakeskus("Riistakeskus", "Viltcentralen"));
    }
}
