package fi.riista.feature.shootingtest.statistics;

import com.google.common.collect.ImmutableSortedMap;
import fi.riista.feature.shootingtest.ShootingTestType;
import fi.riista.feature.shootingtest.statistics.ShootingTestStatisticsRowDTO.TestTypeStatisticsDTO;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static fi.riista.util.Collect.toImmutableSortedMap;
import static fi.riista.util.NumberUtils.sum;
import static java.util.function.Function.identity;

public class ShootingTestStatisticsDTO {

    private final ShootingTestStatisticsRowDTO summary;
    private final List<ShootingTestStatisticsRowDTO> eventStatistics;

    private ShootingTestStatisticsDTO(final List<ShootingTestStatisticsRowDTO> eventStatistics,
                                      final ShootingTestStatisticsRowDTO summary) {

        this.eventStatistics = eventStatistics;
        this.summary = summary;
    }

    public static ShootingTestStatisticsDTO create(final List<ShootingTestStatisticsRowDTO> eventStatistics) {
        return new ShootingTestStatisticsDTO(eventStatistics, calculateSummary(eventStatistics));
    }

    private static ShootingTestStatisticsRowDTO calculateSummary(final List<ShootingTestStatisticsRowDTO> eventStatistics) {

        final ImmutableSortedMap<ShootingTestType, TestTypeStatisticsDTO> testTypeStats = Arrays
                .stream(ShootingTestType.values())
                .collect(toImmutableSortedMap(identity(), t -> {
                    final int total = eventStatistics.stream().mapToInt(e -> e.getTotal(t)).sum();
                    final int qualified = eventStatistics.stream().mapToInt(e -> e.getQualified(t)).sum();

                    return new TestTypeStatisticsDTO(total, qualified);
                }));

        final BigDecimal paid = sum(eventStatistics, ShootingTestStatisticsRowDTO::getPaid);
        final BigDecimal dueAmount = sum(testTypeStats.values(), TestTypeStatisticsDTO::getDueAmount);

        return new ShootingTestStatisticsRowDTO(null, paid, dueAmount, testTypeStats);
    }

    public ShootingTestStatisticsRowDTO getSummary() {
        return summary;
    }

    public List<ShootingTestStatisticsRowDTO> getEventStatistics() {
        return eventStatistics;
    }
}
