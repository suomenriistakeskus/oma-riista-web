package fi.riista.feature.organization.rhy.subsidy2019.compensation;

import com.google.common.collect.Streams;
import fi.riista.feature.organization.rhy.subsidy.SubsidyCalculation;
import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationBasis;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static fi.riista.feature.organization.rhy.subsidy2019.compensation.SubsidyCompensation2019Partition.partitionByCompensationNeed;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public final class SubsidyAllocation2019CompensationRound {

    public static SubsidyAllocation2019CompensationRoundDTO run(@Nonnull final List<SubsidyCompensation2019InputDTO> inputs) {
        final SubsidyCompensation2019Partition partition = partitionByCompensationNeed(inputs);
        final SubsidyAllocationCompensationBasis basis = partition.calculateCompensationBasis();

        final Stream<SubsidyCompensation2019OutputDTO> givenMore = compensate(partition.getNeedingCompensation());

        final Stream<SubsidyCompensation2019OutputDTO> keptUnchanged = keepUnchanged(partition.getKeptUnchanged());

        final Stream<SubsidyCompensation2019OutputDTO> downscaled =
                downscale(partition.getDownscaled(), basis.getDecrementCoefficient());

        final List<SubsidyCompensation2019OutputDTO> results = Streams
                .concat(givenMore, keptUnchanged, downscaled)
                .sorted(comparing(SubsidyCompensation2019OutputDTO::getRhyCode))
                .collect(toList());

        return new SubsidyAllocation2019CompensationRoundDTO(basis, results);
    }

    private static Stream<SubsidyCompensation2019OutputDTO> compensate(final List<SubsidyCompensation2019InputDTO> needingCompensation) {
        return needingCompensation
                .stream()
                .map(input -> SubsidyCompensation2019OutputDTO.increased(input, input.countAmountOfCompensationNeed()));
    }

    private static Stream<SubsidyCompensation2019OutputDTO> keepUnchanged(final List<SubsidyCompensation2019InputDTO> keptUnchanged) {
        return keptUnchanged.stream().map(SubsidyCompensation2019OutputDTO::keepSubsidyUnchanged);
    }

    private static Stream<SubsidyCompensation2019OutputDTO> downscale(final List<SubsidyCompensation2019InputDTO> toBeDownscaled,
                                                                      final BigDecimal decrementMultiplier) {
        return toBeDownscaled
                .stream()
                .map(input -> {
                    final BigDecimal decrement = SubsidyCalculation
                            .calculateDecrement(input.getTotalSubsidyCalculatedForCurrentYear(), decrementMultiplier);

                    return SubsidyCompensation2019OutputDTO.decreased(input, decrement);
                });
    }

    private SubsidyAllocation2019CompensationRound() {
        throw new AssertionError();
    }
}
