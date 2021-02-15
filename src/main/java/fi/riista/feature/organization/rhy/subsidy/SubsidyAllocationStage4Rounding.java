package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationResultDTO;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyCalculation.roundToEvenEuros;
import static fi.riista.util.NumberUtils.currencySum;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

public final class SubsidyAllocationStage4Rounding {

    public static List<RhySubsidyRoundingStage4DTO> roundCompensatedSubsidies(
            @Nonnull final SubsidyAllocationCompensationResultDTO compensationResult) {

        requireNonNull(compensationResult);

        final List<ComparableSubsidyAfterCompensation> comparableCompensationResults =
                getComparableSubsidiesAfterCompensation(compensationResult);

        final BigDecimal totalCalculatedAmountBeforeCompensation =
                compensationResult.getSumOfCalculatedSubsidiesBeforeCompensation();

        final BigDecimal totalCalculatedAmountAfterRoundingDown =
                currencySum(comparableCompensationResults, s -> s.subsidyAfterCompensationRoundedToEvenEuros);

        final int remainderEuros = totalCalculatedAmountBeforeCompensation
                .subtract(totalCalculatedAmountAfterRoundingDown)
                .intValueExact();

        final Map<Boolean, List<ComparableSubsidyAfterCompensation>> partitionByDownscaling =
                comparableCompensationResults.stream().collect(partitioningBy(s -> s.downscaled));

        final Stream<RhySubsidyRoundingStage4DTO> roundingResultsForDownscaled =
                allocateRemainderAndDoRounding(partitionByDownscaling.get(true), remainderEuros);

        final Stream<RhySubsidyRoundingStage4DTO> roundingResultsForOthers =
                partitionByDownscaling.get(false).stream().map(s -> s.toRoundingResult(0));

        return Stream.concat(roundingResultsForDownscaled, roundingResultsForOthers).collect(toList());
    }

    private static List<ComparableSubsidyAfterCompensation> getComparableSubsidiesAfterCompensation(
            final SubsidyAllocationCompensationResultDTO compensationResult) {

        return F.mapNonNullsToList(compensationResult.getAllCompensationOutputsOfLastRound(), lastRoundOutput -> {

            final String rhyCode = lastRoundOutput.getRhyCode();

            final BigDecimal beforeCompensation = compensationResult.getCalculatedSubsidyBeforeCompensation(rhyCode);
            final BigDecimal afterCompensation = lastRoundOutput.getCalculatedSubsidy();

            return new ComparableSubsidyAfterCompensation(
                    rhyCode, beforeCompensation, afterCompensation, lastRoundOutput.isDownscaled());
        });
    }

    private static Stream<RhySubsidyRoundingStage4DTO> allocateRemainderAndDoRounding(
            final List<ComparableSubsidyAfterCompensation> rhysToShareRemainder, final int remainderEuros) {

        final int numRemainderSharers = rhysToShareRemainder.size();
        final int numberOfEurosGivenToAllSharers = remainderEuros / numRemainderSharers;
        final int numSharersGivenOneEuroMore = remainderEuros - numRemainderSharers * numberOfEurosGivenToAllSharers;

        final Stream<RhySubsidyRoundingStage4DTO> firstPartition = rhysToShareRemainder
                .stream()
                .sorted(ComparableSubsidyAfterCompensation.COMPARATOR)
                .limit(numSharersGivenOneEuroMore)
                .map(sharer -> sharer.toRoundingResult(numberOfEurosGivenToAllSharers + 1));

        final Stream<RhySubsidyRoundingStage4DTO> secondPartition = rhysToShareRemainder
                .stream()
                .sorted(ComparableSubsidyAfterCompensation.COMPARATOR)
                .skip(numSharersGivenOneEuroMore)
                .map(sharer -> sharer.toRoundingResult(numberOfEurosGivenToAllSharers));

        return Stream.concat(firstPartition, secondPartition);
    }

    private static class ComparableSubsidyAfterCompensation {

        static final Comparator<ComparableSubsidyAfterCompensation> COMPARATOR = comparing(s -> {
            return s.subsidyAfterCompensationRoundedToEvenEuros.subtract(s.subsidyBeforeCompensation);
        });

        final String rhyCode;

        final BigDecimal subsidyBeforeCompensation;
        final BigDecimal subsidyAfterCompensation;
        final BigDecimal subsidyAfterCompensationRoundedToEvenEuros;

        final boolean downscaled;

        ComparableSubsidyAfterCompensation(@Nonnull final String rhyCode,
                                           @Nonnull final BigDecimal subsidyBeforeCompensation,
                                           @Nonnull final BigDecimal subsidyAfterCompensation,
                                           final boolean downscaled) {

            this.rhyCode = requireNonNull(rhyCode);
            this.subsidyBeforeCompensation = requireNonNull(subsidyBeforeCompensation);
            this.subsidyAfterCompensation = requireNonNull(subsidyAfterCompensation);
            this.downscaled = downscaled;

            this.subsidyAfterCompensationRoundedToEvenEuros =
                    roundToEvenEuros(subsidyAfterCompensation, BigDecimal.ROUND_DOWN);
        }

        public RhySubsidyRoundingStage4DTO toRoundingResult(final int givenRemainderEuros) {
            checkArgument(givenRemainderEuros >= 0, "Remainder must not be negative");

            final BigDecimal resultSubsidy = givenRemainderEuros == 0
                    ? subsidyAfterCompensationRoundedToEvenEuros
                    : subsidyAfterCompensationRoundedToEvenEuros.add(new BigDecimal(givenRemainderEuros));

            return new RhySubsidyRoundingStage4DTO(
                    rhyCode,
                    new SubsidyRoundingDTO(subsidyAfterCompensation, resultSubsidy, givenRemainderEuros));
        }
    }

    private SubsidyAllocationStage4Rounding() {
        throw new AssertionError();
    }
}
