package fi.riista.util;

import com.google.common.base.Preconditions;
import org.joda.time.DateTimeUtils;

import java.util.concurrent.atomic.AtomicLong;

public class MockTimeProvider implements DateTimeUtils.MillisProvider {

    private static final MockTimeProvider INSTANCE = new MockTimeProvider();
    private static AtomicLong timestamp = null;


    public static void mockTime() {
        mockTime(0L);
    }

    public static void mockTime(long initialMillis) {
        timestamp = new AtomicLong(initialMillis);
        DateTimeUtils.setCurrentMillisProvider(INSTANCE);
    }

    public static void advance() {
        Preconditions.checkNotNull(timestamp);
        timestamp.incrementAndGet();
    }

    public static void resetMock() {
        DateTimeUtils.setCurrentMillisSystem();
        timestamp = null;
    }

    @Override
    public long getMillis() {
        Preconditions.checkNotNull(timestamp);
        return timestamp.get();
    }
}
