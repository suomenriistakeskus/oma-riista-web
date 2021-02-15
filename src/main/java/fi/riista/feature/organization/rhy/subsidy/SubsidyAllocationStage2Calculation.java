package fi.riista.feature.organization.rhy.subsidy;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static fi.riista.util.NumberUtils.isNegative;
import static fi.riista.util.NumberUtils.nullableSum;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

// In stage 2, remainder after calculation done in stage 1 is allocated to RHYs.
public final class SubsidyAllocationStage2Calculation {

    private static final Comparator<RhySubsidyStage1DTO> REMAINDER_ORDERING =
            comparing(dto -> dto.getCalculation().getTotalRoundedShare());

    // The remainder euros left unallocated because of rounding effects involved in calculations
    // done in previous stages are allocated here in order to have whole subsidy pot consumed.
    public static List<RhySubsidyStage2DTO> calculateRemainder(@Nonnull final BigDecimal totalSubsidyAmount,
                                                               @Nonnull final List<RhySubsidyStage1DTO> rhyAllocations) {
        requireNonNull(totalSubsidyAmount);
        requireNonNull(rhyAllocations);

        if (rhyAllocations.isEmpty()) {
            throw new IllegalArgumentException("rhyAllocations must not be empty");
        }

        // Allocated euros in previous calculation.
        final BigDecimal alreadyAllocatedMoneySum =
                nullableSum(rhyAllocations, dto -> dto.getCalculation().getTotalRoundedShare());

        // What is left unallocated after previous stages of calculation.
        final BigDecimal subsidyRemainder = totalSubsidyAmount.subtract(alreadyAllocatedMoneySum);

        if (isNegative(subsidyRemainder)) {
            throw new IllegalArgumentException("Subsidy remainder must not be negative");
        }

        // Throws exception if remainder has a non-zero fractional part.
        // In other words, it is expected here that the remainder is an integer.
        final int remainderEuros = subsidyRemainder.intValueExact();

        return allocateRemainder(remainderEuros, rhyAllocations);
    }

    private static List<RhySubsidyStage2DTO> allocateRemainder(final int remainderEuros,
                                                               final List<RhySubsidyStage1DTO> rhyAllocations) {

        final int numRhys = rhyAllocations.size();
        final int numberOfEurosGivenToAll = remainderEuros / numRhys;
        final int numRhysGivenOneEuroMoreThanOthers = remainderEuros - numRhys * numberOfEurosGivenToAll;

        final Stream<RhySubsidyStage2DTO> firstPartition = rhyAllocations
                .stream()
                .sorted(REMAINDER_ORDERING)
                .limit(numRhysGivenOneEuroMoreThanOthers)
                .map(allocation -> new RhySubsidyStage2DTO(allocation, numberOfEurosGivenToAll + 1));

        final Stream<RhySubsidyStage2DTO> secondPartition = rhyAllocations
                .stream()
                .sorted(REMAINDER_ORDERING)
                .skip(numRhysGivenOneEuroMoreThanOthers)
                .map(allocation -> new RhySubsidyStage2DTO(allocation, numberOfEurosGivenToAll));

        return Stream.concat(firstPartition, secondPartition).collect(toList());
    }

    private SubsidyAllocationStage2Calculation() {
        throw new AssertionError();
    }
}
