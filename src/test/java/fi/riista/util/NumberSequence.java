package fi.riista.util;

import java.util.concurrent.atomic.AtomicLong;

public enum NumberSequence implements NumberGenerator {

    INSTANCE;

    private static final long INITIAL_VALUE = 1L;

    private final AtomicLong nextValue = new AtomicLong(INITIAL_VALUE);


    @Override
    public void reset() {
        nextValue.set(INITIAL_VALUE);
    }

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
