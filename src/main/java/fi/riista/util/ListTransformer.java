package fi.riista.util;

import com.google.common.collect.ImmutableList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
    @Transactional(readOnly = true)
    public Page<U> apply(final @Nonnull Page<T> resultPage,
                         final @Nonnull Pageable pageRequest) {
        Objects.requireNonNull(resultPage, "resultPage must not be null");
        Objects.requireNonNull(pageRequest, "pageRequest must not be null");

        final List<U> transformedList = apply(resultPage.getContent());
        final List<U> resultList = transformedList != null ? transformedList : Collections.emptyList();

        return new PageImpl<>(resultList, pageRequest, resultPage.getTotalElements());
    }

    @Nonnull
    public Function<List<T>, List<U>> asFunction() {
        return ListTransformer.this::apply;
    }

    @Nonnull
    public Function<T, U> asSingletonFunction() {
        return ListTransformer.this::apply;
    }

}
