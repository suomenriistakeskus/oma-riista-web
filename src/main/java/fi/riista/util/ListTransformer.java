package fi.riista.util;

import com.google.common.collect.ImmutableList;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Transactional(readOnly = true)
public abstract class ListTransformer<T, U> {

    @Nullable
    public List<U> apply(@Nullable final List<T> list) {
        return list == null ? null : ImmutableList.copyOf(transform(list));
    }

    @Nullable
    public U apply(@Nullable final T object) {
        if (object == null) {
            return null;
        }

        final List<U> singletonList = apply(Collections.singletonList(object));

        if (singletonList == null) {
            return null;
        }

        if (singletonList.size() != 1) {
            throw new IllegalStateException("Expected list containing exactly one element");
        }

        return singletonList.get(0);
    }

    @Nonnull
    protected abstract List<U> transform(@Nonnull List<T> list);

    @Nonnull
    public Function<List<T>, List<U>> asFunction() {
        return ListTransformer.this::apply;
    }

    @Nonnull
    public Function<T, U> asSingletonFunction() {
        return ListTransformer.this::apply;
    }

}
