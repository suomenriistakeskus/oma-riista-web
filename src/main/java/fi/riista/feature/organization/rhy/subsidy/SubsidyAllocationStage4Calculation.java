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

import static fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationResultDTO.noCompensationDone;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

// In stage 4, lower limits imposed by subsidies granted last year are
// calculated. RHYs whose subsidies are falling below calculated lower limits
// are compensated at the expense of other RHYs.
public final class SubsidyAllocationStage4Calculation {

    // RHY specific subsidy will be protected from too rapid decline when compared to lower limit
    // calculated on the basis of subsidy granted last year. Compensation is done at the expense
    // of RHYs exceeding lower limit.
    public static SubsidyAllocationStage4ResultDTO calculateCompensation(
            @Nonnull final List<SubsidyAllocationStage3DTO> rhyAllocations) {

        requireNonNull(rhyAllocations);

        final List<SubsidyCompensationInputDTO> compensationInputs =
                F.mapNonNullsToList(rhyAllocations, SubsidyAllocationStage3DTO::toCompensationInput);

        final SubsidyAllocationCompensationResultDTO compensationResult =
                SubsidyAllocationCompensationCalculation.executeCompensationIfNeeded(compensationInputs);

        if (!compensationResult.isAnyCompensationRoundExecuted()) {
            return createResultWithoutCompensation(rhyAllocations);
        }

        final List<Stage4RoundingResultDTO> roundingResults =
                SubsidyAllocationStage4Rounding.roundCompensatedSubsidies(compensationResult);

        return createCompensatedResult(rhyAllocations, compensationResult, roundingResults);
    }

    private static SubsidyAllocationStage4ResultDTO createResultWithoutCompensation(
            final List<SubsidyAllocationStage3DTO> rhyAllocations) {

        return new SubsidyAllocationStage4ResultDTO(noCompensationDone(), rhyAllocations
                .stream()
                .map(allocation -> toStage4DTO(allocation, null))
                .collect(toList()));
    }

    private static SubsidyAllocationStage4ResultDTO createCompensatedResult(
            final List<SubsidyAllocationStage3DTO> rhyAllocations,
            final SubsidyAllocationCompensationResultDTO compensationResult,
            final List<Stage4RoundingResultDTO> roundingResults) {

        final Map<String, Stage4RoundingResultDTO> roundingResultsByRhyCode =
                F.index(roundingResults, Stage4RoundingResultDTO::getRhyCode);

        final List<SubsidyAllocationStage4DTO> resultAllocations = F.mapNonNullsToList(rhyAllocations, allocation -> {

            final String rhyCode = allocation.getRhyCode();
            final Stage4RoundingResultDTO roundingResult = roundingResultsByRhyCode.get(rhyCode);

            return toStage4DTO(allocation, roundingResult);
        });

        return new SubsidyAllocationStage4ResultDTO(compensationResult, resultAllocations);
    }

    private static SubsidyAllocationStage4DTO toStage4DTO(@Nonnull final SubsidyAllocationStage3DTO allocation,
                                                          @Nullable final Stage4RoundingResultDTO roundingResult) {

        final BigDecimal calculatedSubsidyBeforeFinalRounding;
        final BigDecimal calculatedSubsidyAfterFinalRounding;
        final int givenRemainderEuros;

        if (roundingResult != null) {
            calculatedSubsidyBeforeFinalRounding = roundingResult.getSubsidyBeforeRounding();
            calculatedSubsidyAfterFinalRounding = roundingResult.getSubsidyAfterRounding();
            givenRemainderEuros = roundingResult.getGivenRemainderEuros();
        } else {
            // Use result of previous stage.
            calculatedSubsidyBeforeFinalRounding = allocation.getTotalRoundedShare();
            calculatedSubsidyAfterFinalRounding = allocation.getTotalRoundedShare();

            givenRemainderEuros = 0;
        }

        return new SubsidyAllocationStage4DTO(
                allocation.getRhy(),
                allocation.getRka(),
                allocation.getCalculatedShares(),
                calculatedSubsidyBeforeFinalRounding,
                calculatedSubsidyAfterFinalRounding,
                allocation.getSubsidyBatchInfo(),
                allocation.getRemainderEurosGivenInStage2(),
                givenRemainderEuros);
    }

    private SubsidyAllocationStage4Calculation() {
        throw new AssertionError();
    }
}
