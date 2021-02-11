package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationCalculation;
import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationResultDTO;
import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyCompensationInputDTO;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationResultDTO.noCompensationDone;
import static java.util.Objects.requireNonNull;

// In stage 4, lower limits imposed by subsidies granted last year are
// calculated. RHYs whose subsidies are falling below calculated lower limits
// are compensated at the expense of other RHYs.
public final class SubsidyAllocationStage4Calculation {

    // RHY specific subsidy will be protected from too rapid decline when compared to lower limit
    // calculated on the basis of subsidy granted last year. Compensation is done at the expense
    // of RHYs exceeding lower limit.
    public static SubsidyAllocationStage4ResultDTO calculateCompensation(
            @Nonnull final List<RhySubsidyStage3DTO> rhyAllocations) {

        requireNonNull(rhyAllocations);

        final List<SubsidyCompensationInputDTO> compensationInputs =
                F.mapNonNullsToList(rhyAllocations, RhySubsidyStage3DTO::toCompensationInput);

        final SubsidyAllocationCompensationResultDTO compensationResult =
                SubsidyAllocationCompensationCalculation.executeCompensationIfNeeded(compensationInputs);

        if (!compensationResult.isAnyCompensationRoundExecuted()) {
            return createResultWithoutCompensation(rhyAllocations);
        }

        final List<RhySubsidyRoundingStage4DTO> roundingResults =
                SubsidyAllocationStage4Rounding.roundCompensatedSubsidies(compensationResult);

        return createCompensatedResult(rhyAllocations, compensationResult, roundingResults);
    }

    private static SubsidyAllocationStage4ResultDTO createResultWithoutCompensation(
            final List<RhySubsidyStage3DTO> inputRhyAllocations) {

        final List<RhySubsidyStage4DTO> resultRhyAllocations =
                F.mapNonNullsToList(inputRhyAllocations, allocation -> toStage4DTO(allocation, null));

        return new SubsidyAllocationStage4ResultDTO(resultRhyAllocations, noCompensationDone());
    }

    private static SubsidyAllocationStage4ResultDTO createCompensatedResult(
            final List<RhySubsidyStage3DTO> rhyAllocations,
            final SubsidyAllocationCompensationResultDTO compensationResult,
            final List<RhySubsidyRoundingStage4DTO> roundingResults) {

        final Map<String, RhySubsidyRoundingStage4DTO> roundingResultsByRhyCode =
                F.index(roundingResults, RhySubsidyRoundingStage4DTO::getRhyCode);

        final List<RhySubsidyStage4DTO> resultAllocations = F.mapNonNullsToList(rhyAllocations, allocation -> {

            final String rhyCode = allocation.getRhyCode();
            final RhySubsidyRoundingStage4DTO roundingResult = roundingResultsByRhyCode.get(rhyCode);

            return toStage4DTO(allocation, roundingResult);
        });

        return new SubsidyAllocationStage4ResultDTO(resultAllocations, compensationResult);
    }

    private static RhySubsidyStage4DTO toStage4DTO(@Nonnull final RhySubsidyStage3DTO stage3Allocation,
                                                   @Nullable final RhySubsidyRoundingStage4DTO rhyRoundingResult) {

        final SubsidyRoundingDTO rounding = Optional
                .ofNullable(rhyRoundingResult)
                .map(RhySubsidyRoundingStage4DTO::getRoundingResult)
                .orElseGet(() -> {

                    final BigDecimal statisticsBasedSubsidy =
                            stage3Allocation.getCalculation().getSubsidyAfterStage2RemainderAllocation();

                    return new SubsidyRoundingDTO(statisticsBasedSubsidy, statisticsBasedSubsidy, 0);
                });

        return new RhySubsidyStage4DTO(stage3Allocation, rounding);
    }

    private SubsidyAllocationStage4Calculation() {
        throw new AssertionError();
    }
}
