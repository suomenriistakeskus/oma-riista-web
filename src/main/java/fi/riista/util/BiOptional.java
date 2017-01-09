package fi.riista.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public final class BiOptional<A, B> {
    private final Optional<A> a;
    private final Optional<B> b;

    private BiOptional(final Optional<A> a, final Optional<B> b) {
        this.a = Objects.requireNonNull(a);
        this.b = Objects.requireNonNull(b);
    }

    public static <A, B> BiOptional<A, B> create() {
        return new BiOptional<>(Optional.empty(), Optional.empty());
    }

    public <C> BiOptional<C, B> left(final Optional<C> c) {
        return new BiOptional<>(c, this.b);
    }

    public <C> BiOptional<A, C> right(final Optional<C> c) {
        return new BiOptional<>(this.a, c);
    }

    public void consumeBoth(final BiConsumer<A, B> consumer) {
        if (a.isPresent() && b.isPresent()) {
            consumer.accept(a.get(), b.get());
        }
    }
}
