package fi.riista.feature.organization.rhy.subsidy2019;

import fi.riista.feature.organization.rhy.subsidy.RhySubsidyRoundingStage4DTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyRoundingDTO;
import fi.riista.feature.organization.rhy.subsidy2019.compensation.SubsidyAllocation2019CompensationCalculation;
import fi.riista.feature.organization.rhy.subsidy2019.compensation.SubsidyAllocation2019CompensationResultDTO;
import fi.riista.feature.organization.rhy.subsidy2019.compensation.SubsidyCompensation2019InputDTO;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fi.riista.feature.organization.rhy.subsidy2019.compensation.SubsidyAllocation2019CompensationResultDTO.noCompensationDone;
import static java.util.Objects.requireNonNull;

// In stage 4, lower limits imposed by subsidies granted last year are
// calculated. RHYs whose subsidies are falling below calculated lower limits
// are compensated at the expense of other RHYs.
public final class SubsidyAllocation2019Stage4Calculation {

    // RHY specific subsidy will be protected from too rapid decline when compared to lower limit
    // calculated on the basis of subsidy granted last year. Compensation is done at the expense
    // of RHYs exceeding lower limit.
    public static SubsidyAllocation2019ResultDTO calculateCompensation(
            @Nonnull final List<SubsidyAllocation2019Stage3DTO> rhyAllocations) {

        requireNonNull(rhyAllocations);

        final List<SubsidyCompensation2019InputDTO> compensationInputs =
                F.mapNonNullsToList(rhyAllocations, SubsidyAllocation2019Stage3DTO::toCompensationInput);

        final SubsidyAllocation2019CompensationResultDTO compensationResult =
                SubsidyAllocation2019CompensationCalculation.executeCompensationIfNeeded(compensationInputs);

        if (!compensationResult.isAnyCompensationRoundExecuted()) {
            return createResultWithoutCompensation(rhyAllocations);
        }

        final List<RhySubsidyRoundingStage4DTO> roundingResults =
                SubsidyAllocation2019Stage4Rounding.roundCompensatedSubsidies(compensationResult);

        return createCompensatedResult(rhyAllocations, compensationResult, roundingResults);
    }

    private static SubsidyAllocation2019ResultDTO createResultWithoutCompensation(
            final List<SubsidyAllocation2019Stage3DTO> inputRhyAllocations) {

        final List<SubsidyAllocation2019Stage4DTO> resultRhyAllocations =
                F.mapNonNullsToList(inputRhyAllocations, allocation -> toStage4DTO(allocation, null));

        return new SubsidyAllocation2019ResultDTO(resultRhyAllocations, noCompensationDone());
    }

    private static SubsidyAllocation2019ResultDTO createCompensatedResult(
            final List<SubsidyAllocation2019Stage3DTO> rhyAllocations,
            final SubsidyAllocation2019CompensationResultDTO compensationResult,
            final List<RhySubsidyRoundingStage4DTO> roundingResults) {

        final Map<String, RhySubsidyRoundingStage4DTO> roundingResultsByRhyCode =
                F.index(roundingResults, RhySubsidyRoundingStage4DTO::getRhyCode);

        final List<SubsidyAllocation2019Stage4DTO> resultAllocations = F.mapNonNullsToList(rhyAllocations, allocation -> {

            final String rhyCode = allocation.getRhyCode();
            final RhySubsidyRoundingStage4DTO roundingResult = roundingResultsByRhyCode.get(rhyCode);

            return toStage4DTO(allocation, roundingResult);
        });

        return new SubsidyAllocation2019ResultDTO(resultAllocations, compensationResult);
    }

    private static SubsidyAllocation2019Stage4DTO toStage4DTO(@Nonnull final SubsidyAllocation2019Stage3DTO allocation,
                                                              @Nullable final RhySubsidyRoundingStage4DTO rhyRoundingResult) {

        final SubsidyRoundingDTO rounding = Optional
                .ofNullable(rhyRoundingResult)
                .map(RhySubsidyRoundingStage4DTO::getRoundingResult)
                .orElseGet(() -> {

                    final BigDecimal statisticsBasedSubsidy = allocation.getTotalRoundedShare();

                    return new SubsidyRoundingDTO(statisticsBasedSubsidy, statisticsBasedSubsidy, 0);
                });

        return new SubsidyAllocation2019Stage4DTO(
                allocation.getRhy(),
                allocation.getRka(),
                allocation.getCalculatedShares(),
                allocation.getRemainderEurosGivenInStage2(),
                allocation.getSubsidyBatchInfo(),
                rounding);
    }

    private SubsidyAllocation2019Stage4Calculation() {
        throw new AssertionError();
    }
}
