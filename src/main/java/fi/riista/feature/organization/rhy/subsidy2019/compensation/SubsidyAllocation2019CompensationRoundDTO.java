package fi.riista.feature.organization.rhy.subsidy2019.compensation;

import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationBasis;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Objects.requireNonNull;

// Models a result of single compensation round. Multiple rounds may be needed
// to have all RHY subsidies be raised to lower limit (calculated on the basis
// of subsidy granted last year).
public class SubsidyAllocation2019CompensationRoundDTO {

    private final SubsidyAllocationCompensationBasis basis;

    private final List<SubsidyCompensation2019OutputDTO> resultingSubsidies;

    public SubsidyAllocation2019CompensationRoundDTO(@Nonnull final SubsidyAllocationCompensationBasis basis,
                                                     @Nonnull final List<SubsidyCompensation2019OutputDTO> resultingSubsidies) {

        this.basis = requireNonNull(basis);
        this.resultingSubsidies = requireNonNull(resultingSubsidies);
    }

    public boolean isAnotherRoundNeeded() {
        return resultingSubsidies.stream().anyMatch(SubsidyCompensation2019OutputDTO::isCompensationNeeded);
    }

    public List<SubsidyCompensation2019InputDTO> transformToInputForAnotherRound() {
        return F.mapNonNullsToList(
                resultingSubsidies, SubsidyCompensation2019OutputDTO::toInputForAnotherCompensationRound);
    }

    // Accessors -->

    public SubsidyAllocationCompensationBasis getBasis() {
        return basis;
    }

    public List<SubsidyCompensation2019OutputDTO> getResultingSubsidies() {
        return resultingSubsidies;
    }
}
