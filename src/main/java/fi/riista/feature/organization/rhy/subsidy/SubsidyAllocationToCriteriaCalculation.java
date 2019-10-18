package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportDTO;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationUnitAmountCalculation.calculateUnitAmountsForSubsidyCriteria;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyCalculation.roundSubsidyAmountAllocatedToCriterion;
import static fi.riista.util.Collect.toMap;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

public final class SubsidyAllocationToCriteriaCalculation {

    public static LinkedHashMap<SubsidyAllocationCriterion, BigDecimal> calculateSubsidyAmountForEachCriterion(
            @Nonnull final BigDecimal totalSubsidyAmount,
            final int year) {

        requireNonNull(totalSubsidyAmount);

        final Function<SubsidyAllocationCriterion, BigDecimal> calculateTotalSubsidyAmountForCriterion = criterion -> {
            final BigDecimal multiplier = criterion.getPercentageShare().movePointLeft(2);
            return roundSubsidyAmountAllocatedToCriterion(totalSubsidyAmount.multiply(multiplier));
        };

        return SubsidyAllocationCriterion
                .getSubsidyCriteria(year)
                .stream()
                .collect(toMap(identity(), calculateTotalSubsidyAmountForCriterion, LinkedHashMap::new));
    }

    public static List<SubsidyAllocatedToCriterionDTO> calculateAllocationOfRhySubsidyToEachCriterion(
            @Nonnull final List<AnnualStatisticsExportDTO> allRhyStatistics,
            @Nonnull final BigDecimal totalSubsidyAmount,
            final int year) {

        final LinkedHashMap<SubsidyAllocationCriterion, BigDecimal> subsidyAmountAllocatedToEachCriterion =
                calculateSubsidyAmountForEachCriterion(totalSubsidyAmount, year);

        final Map<SubsidyAllocationCriterion, BigDecimal> unitAmounts =
                calculateUnitAmountsForSubsidyCriteria(allRhyStatistics, subsidyAmountAllocatedToEachCriterion);

        return subsidyAmountAllocatedToEachCriterion.entrySet()
                .stream()
                .map(entry -> {

                    final SubsidyAllocationCriterion criterion = entry.getKey();
                    final BigDecimal allocatedAmount = entry.getValue();

                    return new SubsidyAllocatedToCriterionDTO(criterion, allocatedAmount, unitAmounts.get(criterion));
                })
                .collect(toList());
    }

    private SubsidyAllocationToCriteriaCalculation() {
        throw new AssertionError();
    }
}
