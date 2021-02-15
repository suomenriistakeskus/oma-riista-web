package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationResultDTO;
import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationRoundDTO;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class SubsidyAllocationStage4ResultDTO {

    private final List<RhySubsidyStage4DTO> rhyAllocations;

    private final SubsidyAllocationCompensationResultDTO compensationResult;

    public SubsidyAllocationStage4ResultDTO(@Nonnull final List<RhySubsidyStage4DTO> rhyAllocations,
                                            @Nonnull final SubsidyAllocationCompensationResultDTO compensationResult) {

        this.rhyAllocations = requireNonNull(rhyAllocations);
        this.compensationResult = requireNonNull(compensationResult);

        final int numRhyAllocations = rhyAllocations.size();

        for (final SubsidyAllocationCompensationRoundDTO round : compensationResult.getRounds()) {
            checkArgument(numRhyAllocations == round.getResultingSubsidies().size());
        }
    }

    public List<RhySubsidyStage4DTO> getRhyAllocations() {
        return rhyAllocations;
    }

    public SubsidyAllocationCompensationResultDTO getCompensationResult() {
        return compensationResult;
    }
}
