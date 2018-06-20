package fi.riista.feature.shootingtest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.SortedMap;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class ShootingTestStatisticsRowDTO {

    public static class TestTypeStatisticsDTO {
        public static final TestTypeStatisticsDTO EMPTY = new TestTypeStatisticsDTO(0, 0);

        private final int total;
        private final int qualified;
        private final int unqualified;
        private final Double qualifiedPercentage;
        private final BigDecimal dueAmount;

        public TestTypeStatisticsDTO(final int total, final int qualified) {
            checkArgument(total >= 0 && qualified >= 0, "total and qualified must not be negative");
            checkArgument(total >= qualified, "total must not be lower than qualified");

            this.total = total;
            this.qualified = qualified;
            this.unqualified = total - qualified;
            this.qualifiedPercentage = total > 0 ? (double) qualified / total * 100 : null;
            this.dueAmount = ShootingTestAttempt.calculatePaymentSum(total);
        }

        public int getTotal() {
            return total;
        }

        public int getQualified() {
            return qualified;
        }

        public int getUnqualified() {
            return unqualified;
        }

        public Double getQualifiedPercentage() {
            return qualifiedPercentage;
        }

        public BigDecimal getDueAmount() {
            return dueAmount;
        }
    }

    private final ShootingTestCalendarEventDTO calendarEvent;
    private final BigDecimal paid;
    private final BigDecimal dueAmount;
    private final SortedMap<ShootingTestType, TestTypeStatisticsDTO> testTypes;

    public ShootingTestStatisticsRowDTO(@Nullable final ShootingTestCalendarEventDTO calendarEvent,
                                        @Nonnull final BigDecimal paid,
                                        @Nonnull final BigDecimal dueAmount,
                                        @Nonnull final SortedMap<ShootingTestType, TestTypeStatisticsDTO> testTypes) {

        // calendarEvent is null in case of summary
        this.calendarEvent = calendarEvent;
        this.paid = requireNonNull(paid, "paid is null");
        this.dueAmount = requireNonNull(dueAmount, "dueAmount is null");
        this.testTypes = requireNonNull(testTypes, "testTypes is null");
    }

    public int getTotal(final ShootingTestType testType) {
        final TestTypeStatisticsDTO dto = testTypes.get(testType);
        return dto != null ? dto.getTotal() : 0;
    }

    public int getQualified(final ShootingTestType testType) {
        final TestTypeStatisticsDTO dto = testTypes.get(testType);
        return dto != null ? dto.getQualified() : 0;
    }

    // Accessors -->

    public ShootingTestCalendarEventDTO getCalendarEvent() {
        return calendarEvent;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public BigDecimal getDueAmount() {
        return dueAmount;
    }

    public SortedMap<ShootingTestType, TestTypeStatisticsDTO> getTestTypes() {
        return testTypes;
    }
}
