package fi.riista.util;

import java.util.concurrent.atomic.AtomicLong;

public enum NumberSequence implements NumberGenerator {

    INSTANCE;

    private final AtomicLong nextValue = new AtomicLong(1L);

    @Override
    public int nextInt() {
        return getNextLongAndIncrement().intValue();
    }

    @Override
    public long nextLong() {
        return getNextLongAndIncrement();
    }

    @Override
    public double nextDouble() {
        return getNextLongAndIncrement().doubleValue();
    }

    private Long getNextLongAndIncrement() {
        return nextValue.getAndIncrement();
    }

}
