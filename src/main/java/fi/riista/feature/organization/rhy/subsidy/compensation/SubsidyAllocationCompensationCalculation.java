package fi.riista.feature.organization.rhy.subsidy.compensation;

import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toMap;

public final class SubsidyAllocationCompensationCalculation {

    private static final int MAX_COMPENSATION_ROUNDS_ATTEMPTED = 10;

    public static SubsidyAllocationCompensationResultDTO executeCompensationIfNeeded(
            @Nonnull final List<SubsidyCompensationInputDTO> inputs) {

        checkArgument(!F.isNullOrEmpty(inputs), "inputs must not be null or empty");
        checkInputsAreValid(inputs);

        if (!isCompensationNeeded(inputs)) {
            return SubsidyAllocationCompensationResultDTO.noCompensationDone();
        }

        return new SubsidyAllocationCompensationResultDTO(
                compensate(inputs), createMappingOfCalculatedSubsidiesBeforeCompensation(inputs));
    }

    private static void checkInputsAreValid(final Collection<SubsidyCompensationInputDTO> inputs) {
        if (inputs.stream().anyMatch(SubsidyCompensationInputDTO::isAlreadyCompensated)) {
            throw new IllegalArgumentException("Some inputs already marked compensated");
        }
    }

    private static boolean isCompensationNeeded(final Collection<SubsidyCompensationInputDTO> inputs) {
        return inputs.stream().anyMatch(SubsidyCompensationInputDTO::isCompensationNeeded);
    }

    private static Map<String, BigDecimal> createMappingOfCalculatedSubsidiesBeforeCompensation(
            final List<SubsidyCompensationInputDTO> inputs) {

        return inputs.stream().collect(toMap(
                SubsidyCompensationInputDTO::getRhyCode, SubsidyCompensationInputDTO::getCalculatedSubsidy));
    }

    private static List<SubsidyAllocationCompensationRoundDTO> compensate(List<SubsidyCompensationInputDTO> inputs) {
        final List<SubsidyAllocationCompensationRoundDTO> rounds = new ArrayList<>(MAX_COMPENSATION_ROUNDS_ATTEMPTED);

        while (true) {
            if (rounds.size() >= MAX_COMPENSATION_ROUNDS_ATTEMPTED) {
                throw new IllegalStateException("Too many rounds needed to compensate RHY subsidies");
            }

            final SubsidyAllocationCompensationRoundDTO round = SubsidyAllocationCompensationRound.run(inputs);
            rounds.add(round);

            if (round.isAnotherRoundNeeded()) {
                inputs = round.transformToInputForAnotherRound();
            } else {
                break;
            }
        }

        return rounds;
    }

    private SubsidyAllocationCompensationCalculation() {
        throw new AssertionError();
    }
}
