package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationResultDTO;
import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationRoundDTO;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class SubsidyAllocationStage4ResultDTO {

    private final SubsidyAllocationCompensationResultDTO compensationResult;

    private final List<SubsidyAllocationStage4DTO> rhyAllocations;

    public SubsidyAllocationStage4ResultDTO(@Nonnull final SubsidyAllocationCompensationResultDTO compensationResult,
                                            @Nonnull final List<SubsidyAllocationStage4DTO> rhyAllocations) {

        this.compensationResult = requireNonNull(compensationResult);
        this.rhyAllocations = requireNonNull(rhyAllocations);

        final int numRhyAllocations = rhyAllocations.size();

        for (final SubsidyAllocationCompensationRoundDTO round : compensationResult.getRounds()) {
            checkArgument(numRhyAllocations == round.getResultingSubsidies().size());
        }
    }

    public SubsidyAllocationCompensationResultDTO getCompensationResult() {
        return compensationResult;
    }

    public List<SubsidyAllocationStage4DTO> getRhyAllocations() {
        return rhyAllocations;
    }
}
