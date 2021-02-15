package fi.riista.feature.organization.rhy.subsidy2019.compensation;

import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationBasis;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.Collect.indexingBy;
import static fi.riista.util.NumberUtils.currencySum;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;

// Models a result of subsidy compensation which may consist of zero, one or
// many rounds.
public class SubsidyAllocation2019CompensationResultDTO {

    private static class RhyEntry {

        final String rhyCode;

        final BigDecimal calculatedSubsidyBeforeCompensation;

        final List<SubsidyCompensation2019OutputDTO> outputs;

        RhyEntry(@Nonnull final String rhyCode,
                 @Nonnull final BigDecimal calculatedSubsidyBeforeCompensation,
                 @Nonnull final List<SubsidyCompensation2019OutputDTO> outputs) {

            this.rhyCode = requireNonNull(rhyCode);
            this.calculatedSubsidyBeforeCompensation = requireNonNull(calculatedSubsidyBeforeCompensation);
            this.outputs = requireNonNull(outputs);
        }
    }

    private final int numberOfRounds;

    private final List<SubsidyAllocation2019CompensationRoundDTO> rounds;

    private final Map<String, RhyEntry> rhyEntries;

    public static SubsidyAllocation2019CompensationResultDTO noCompensationDone() {
        return new SubsidyAllocation2019CompensationResultDTO(emptyList(), emptyMap());
    }

    public SubsidyAllocation2019CompensationResultDTO(@Nonnull final List<SubsidyAllocation2019CompensationRoundDTO> rounds,
                                                      @Nonnull final Map<String, BigDecimal> calculatedSubsidiesBeforeCompensation) {

        this.rounds = requireNonNull(rounds);

        this.numberOfRounds = rounds.size();

        if (numberOfRounds == 0) {
            this.rhyEntries = emptyMap();

        } else {
            final Map<String, List<SubsidyCompensation2019OutputDTO>> rhyGroupedOutputs = rounds
                    .stream()
                    .flatMap(dto -> dto.getResultingSubsidies().stream())
                    .collect(groupingBy(SubsidyCompensation2019OutputDTO::getRhyCode));

            final Set<String> rhyCodes = calculatedSubsidiesBeforeCompensation.keySet();

            checkArgument(rhyCodes.equals(rhyGroupedOutputs.keySet()), "RHY codes do not match");

            this.rhyEntries = rhyCodes
                    .stream()
                    .map(rhyCode -> {

                        final BigDecimal subsidyBeforeCompensation = calculatedSubsidiesBeforeCompensation.get(rhyCode);

                        return new RhyEntry(rhyCode, subsidyBeforeCompensation, rhyGroupedOutputs.get(rhyCode));
                    })
                    .collect(indexingBy(e -> e.rhyCode));
        }
    }

    public boolean isAnyCompensationRoundExecuted() {
        return numberOfRounds > 0;
    }

    public List<SubsidyAllocationCompensationBasis> getCompensationBases() {
        return F.mapNonNullsToList(rounds, SubsidyAllocation2019CompensationRoundDTO::getBasis);
    }

    public BigDecimal getCalculatedSubsidyBeforeCompensation(final String rhyCode) {
        return rhyEntries.get(rhyCode).calculatedSubsidyBeforeCompensation;
    }

    public List<SubsidyCompensation2019OutputDTO> getCompensationOutputs(final String rhyCode) {
        return rhyEntries.get(rhyCode).outputs;
    }

    public List<SubsidyCompensation2019OutputDTO> getAllCompensationOutputsOfLastRound() {
        return rounds.get(numberOfRounds - 1).getResultingSubsidies();
    }

    public BigDecimal getSumOfCalculatedSubsidiesBeforeCompensation() {
        return currencySum(rhyEntries.values(), e -> e.calculatedSubsidyBeforeCompensation);
    }

    // Accessors -->

    public int getNumberOfRounds() {
        return numberOfRounds;
    }

    public List<SubsidyAllocation2019CompensationRoundDTO> getRounds() {
        return rounds;
    }
}
