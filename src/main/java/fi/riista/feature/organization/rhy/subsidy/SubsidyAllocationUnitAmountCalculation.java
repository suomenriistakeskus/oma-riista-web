package fi.riista.feature.organization.rhy.subsidy;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportDTO;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportDTO.aggregate;
import static java.util.Objects.requireNonNull;

public final class SubsidyAllocationUnitAmountCalculation {

    public static Map<SubsidyAllocationCriterion, BigDecimal> calculateUnitAmountsForSubsidyCriteria(
            @Nonnull final List<AnnualStatisticsExportDTO> allRhyStatistics,
            @Nonnull final Map<SubsidyAllocationCriterion, BigDecimal> subsidyAmountsAllocatedToCriteria) {

        requireNonNull(allRhyStatistics);
        requireNonNull(subsidyAmountsAllocatedToCriteria);
        checkArgument(!allRhyStatistics.isEmpty(), "allRhyStatistics must not be empty");

        final AnnualStatisticsExportDTO summary = aggregate(allRhyStatistics);

        final ImmutableMap.Builder<SubsidyAllocationCriterion, BigDecimal> builder =
                ImmutableMap.<SubsidyAllocationCriterion, BigDecimal> builder();

        subsidyAmountsAllocatedToCriteria.forEach((criterion, amountAllocatedToCriterion) -> {

            final Integer sumOfQuantities = criterion.getRelatedStatisticItem().extractInteger(summary);

            if (sumOfQuantities == null) {
                throw new IllegalStateException(
                        "Cannot calculate unit amount when sum of all quantities is null: " + criterion.name());
            }

            if (sumOfQuantities == 0) {
                throw new IllegalStateException(
                        "Cannot calculate unit amount when sum of all quantities is zero: " + criterion.name());
            }

            final BigDecimal unitAmount =
                    SubsidyCalculation.divide(amountAllocatedToCriterion, new BigDecimal(sumOfQuantities));

            builder.put(criterion, unitAmount);
        });

        return builder.build();
    }

    private SubsidyAllocationUnitAmountCalculation() {
        throw new AssertionError();
    }
}
