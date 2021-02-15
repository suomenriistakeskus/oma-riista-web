package fi.riista.feature.organization.rhy.subsidy2019;

import fi.riista.feature.organization.rhy.subsidy2019.compensation.SubsidyAllocation2019CompensationResultDTO;
import fi.riista.feature.organization.rhy.subsidy2019.compensation.SubsidyAllocation2019CompensationRoundDTO;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class SubsidyAllocation2019ResultDTO {

    private final List<SubsidyAllocation2019Stage4DTO> rhyAllocations;

    private final SubsidyAllocation2019CompensationResultDTO compensationResult;

    public SubsidyAllocation2019ResultDTO(@Nonnull final List<SubsidyAllocation2019Stage4DTO> rhyAllocations,
                                          @Nonnull final SubsidyAllocation2019CompensationResultDTO compensationResult) {

        this.rhyAllocations = requireNonNull(rhyAllocations);
        this.compensationResult = requireNonNull(compensationResult);

        final int numRhyAllocations = rhyAllocations.size();

        for (final SubsidyAllocation2019CompensationRoundDTO round : compensationResult.getRounds()) {
            checkArgument(numRhyAllocations == round.getResultingSubsidies().size());
        }
    }

    public List<SubsidyAllocation2019Stage4DTO> getRhyAllocations() {
        return rhyAllocations;
    }

    public SubsidyAllocation2019CompensationResultDTO getCompensationResult() {
        return compensationResult;
    }
}
