package fi.riista.feature.organization.rhy.subsidy2019.compensation;

import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toMap;

public final class SubsidyAllocation2019CompensationCalculation {

    private static final int MAX_COMPENSATION_ROUNDS_ATTEMPTED = 10;

    public static SubsidyAllocation2019CompensationResultDTO executeCompensationIfNeeded(
            @Nonnull final List<SubsidyCompensation2019InputDTO> inputs) {

        checkArgument(!F.isNullOrEmpty(inputs), "inputs must not be null or empty");
        checkInputsAreValid(inputs);

        if (!isCompensationNeeded(inputs)) {
            return SubsidyAllocation2019CompensationResultDTO.noCompensationDone();
        }

        return new SubsidyAllocation2019CompensationResultDTO(
                compensate(inputs), createMappingOfCalculatedSubsidiesBeforeCompensation(inputs));
    }

    private static void checkInputsAreValid(final Collection<SubsidyCompensation2019InputDTO> inputs) {
        if (inputs.stream().anyMatch(SubsidyCompensation2019InputDTO::isAlreadyCompensated)) {
            throw new IllegalArgumentException("Some inputs already marked compensated");
        }
    }

    private static boolean isCompensationNeeded(final Collection<SubsidyCompensation2019InputDTO> inputs) {
        return inputs.stream().anyMatch(SubsidyCompensation2019InputDTO::isCompensationNeeded);
    }

    private static Map<String, BigDecimal> createMappingOfCalculatedSubsidiesBeforeCompensation(
            final List<SubsidyCompensation2019InputDTO> inputs) {

        return inputs.stream().collect(toMap(
                SubsidyCompensation2019InputDTO::getRhyCode, SubsidyCompensation2019InputDTO::getCalculatedSubsidy));
    }

    private static List<SubsidyAllocation2019CompensationRoundDTO> compensate(List<SubsidyCompensation2019InputDTO> inputs) {
        final List<SubsidyAllocation2019CompensationRoundDTO> rounds = new ArrayList<>(MAX_COMPENSATION_ROUNDS_ATTEMPTED);

        while (true) {
            if (rounds.size() >= MAX_COMPENSATION_ROUNDS_ATTEMPTED) {
                throw new IllegalStateException("Too many rounds needed to compensate RHY subsidies");
            }

            final SubsidyAllocation2019CompensationRoundDTO round = SubsidyAllocation2019CompensationRound.run(inputs);
            rounds.add(round);

            if (round.isAnotherRoundNeeded()) {
                inputs = round.transformToInputForAnotherRound();
            } else {
                break;
            }
        }

        return rounds;
    }

    private SubsidyAllocation2019CompensationCalculation() {
        throw new AssertionError();
    }
}
