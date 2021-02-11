package fi.riista.feature.organization.rhy.subsidy;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;

import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationConstants.FIRST_BATCH_SHARE_COEFFICIENT;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyCalculation.roundToEvenEuros;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public final class SubsidyAllocationStage5Calculation {

    public static List<RhySubsidyStage5DTO> divideIntoTwoBatches(@Nonnull final List<RhySubsidyStage4DTO> rhyAllocations) {
        return requireNonNull(rhyAllocations)
                .stream()
                .map(stage4Allocation -> {
                    final BigDecimal totalSubsidyAmount =
                            stage4Allocation.getCalculation().getStage4Rounding().getSubsidyAfterRounding();

                    final BigDecimal firstBatchAmount = totalSubsidyAmount.multiply(FIRST_BATCH_SHARE_COEFFICIENT);

                    final BigDecimal firstBatchAmountRoundedToEvenEuros =
                            roundToEvenEuros(firstBatchAmount, BigDecimal.ROUND_DOWN);

                    final BigDecimal secondBatchAmount =
                            totalSubsidyAmount.subtract(firstBatchAmountRoundedToEvenEuros);

                    return new RhySubsidyStage5DTO(stage4Allocation, firstBatchAmountRoundedToEvenEuros, secondBatchAmount);
                })
                .collect(toList());
    }
}
