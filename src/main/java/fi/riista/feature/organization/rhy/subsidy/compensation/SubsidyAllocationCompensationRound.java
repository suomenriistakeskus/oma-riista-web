package fi.riista.feature.organization.rhy.subsidy.compensation;

import com.google.common.collect.Streams;
import fi.riista.feature.organization.rhy.subsidy.SubsidyCalculation;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyCompensationPartition.partitionByCompensationNeed;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public final class SubsidyAllocationCompensationRound {

    public static SubsidyAllocationCompensationRoundDTO run(@Nonnull final List<SubsidyCompensationInputDTO> inputs) {
        final SubsidyCompensationPartition partition = partitionByCompensationNeed(inputs);
        final SubsidyAllocationCompensationBasis basis = partition.calculateCompensationBasis();

        final Stream<SubsidyCompensationOutputDTO> givenMore = compensate(partition.getNeedingCompensation());

        final Stream<SubsidyCompensationOutputDTO> keptUnchanged = keepUnchanged(partition.getKeptUnchanged());

        final Stream<SubsidyCompensationOutputDTO> downscaled =
                downscale(partition.getDownscaled(), basis.getDecrementCoefficient());

        final List<SubsidyCompensationOutputDTO> results = Streams
                .concat(givenMore, keptUnchanged, downscaled)
                .sorted(comparing(SubsidyCompensationOutputDTO::getRhyCode))
                .collect(toList());

        return new SubsidyAllocationCompensationRoundDTO(basis, results);
    }

    private static Stream<SubsidyCompensationOutputDTO> compensate(final List<SubsidyCompensationInputDTO> needingCompensation) {
        return needingCompensation
                .stream()
                .map(input -> SubsidyCompensationOutputDTO.increased(input, input.countAmountOfCompensationNeed()));
    }

    private static Stream<SubsidyCompensationOutputDTO> keepUnchanged(final List<SubsidyCompensationInputDTO> keptUnchanged) {
        return keptUnchanged.stream().map(SubsidyCompensationOutputDTO::keepSubsidyUnchanged);
    }

    private static Stream<SubsidyCompensationOutputDTO> downscale(final List<SubsidyCompensationInputDTO> toBeDownscaled,
                                                                  final BigDecimal decrementMultiplier) {
        return toBeDownscaled
                .stream()
                .map(input -> {
                    final BigDecimal decrement = SubsidyCalculation
                            .calculateDecrement(input.getTotalSubsidyCalculatedForCurrentYear(), decrementMultiplier);

                    return SubsidyCompensationOutputDTO.decreased(input, decrement);
                });
    }

    private SubsidyAllocationCompensationRound() {
        throw new AssertionError();
    }
}
