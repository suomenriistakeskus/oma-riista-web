package fi.riista.util;

import com.google.common.base.Preconditions;
import org.joda.time.DateTimeUtils;
import org.joda.time.ReadablePeriod;

import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkState;

public class MockTimeProvider implements DateTimeUtils.MillisProvider {

    private static final MockTimeProvider INSTANCE = new MockTimeProvider();
    private static AtomicLong timestamp = null;

    public static void assertMockNotActive() {
        checkState(timestamp == null);
    }

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

    public static void advance(final long millis) {
        Preconditions.checkNotNull(timestamp);
        timestamp.addAndGet(millis);
    }

    public static void advance(final ReadablePeriod period) {
        advance(period.toPeriod().toStandardDuration().getMillis());
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
