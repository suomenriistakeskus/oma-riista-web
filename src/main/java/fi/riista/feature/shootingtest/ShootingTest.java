package fi.riista.feature.shootingtest;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePeriod;
import org.joda.time.Years;

import java.math.BigDecimal;

import static fi.riista.util.DateUtil.today;

public final class ShootingTest {

    public static final ReadablePeriod VALIDITY_PERIOD = Years.THREE;
    public static final int MAX_ATTEMPTS_PER_TYPE = 5;
    public static final BigDecimal ATTEMPT_PRICE = BigDecimal.valueOf(20.0);
    public static final Days DAYS_OF_EVENT_UPDATEABLE_BY_OFFICIAL = Days.days(7);

    public static LocalDate getBeginDateOfShootingTestEventList(final boolean shortList) {
        final LocalDate today = today();
        return shortList ? today.minus(DAYS_OF_EVENT_UPDATEABLE_BY_OFFICIAL) : today.withDayOfYear(1);
    }

    private ShootingTest() {
        throw new AssertionError();
    }
}
