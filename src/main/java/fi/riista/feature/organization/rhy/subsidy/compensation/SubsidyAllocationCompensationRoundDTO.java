package fi.riista.feature.organization.rhy.subsidy.compensation;

import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Objects.requireNonNull;

// Models a result of single compensation round. Multiple rounds may be needed
// to have all RHY subsidies be raised to lower limit (calculated on the basis
// of subsidy granted last year).
public class SubsidyAllocationCompensationRoundDTO {

    private final SubsidyAllocationCompensationBasis basis;

    private final List<SubsidyCompensationOutputDTO> resultingSubsidies;

    public SubsidyAllocationCompensationRoundDTO(@Nonnull final SubsidyAllocationCompensationBasis basis,
                                                 @Nonnull final List<SubsidyCompensationOutputDTO> resultingSubsidies) {

        this.basis = requireNonNull(basis);
        this.resultingSubsidies = requireNonNull(resultingSubsidies);
    }

    public boolean isAnotherRoundNeeded() {
        return resultingSubsidies.stream().anyMatch(SubsidyCompensationOutputDTO::isCompensationNeeded);
    }

    public List<SubsidyCompensationInputDTO> transformToInputForAnotherRound() {
        return F.mapNonNullsToList(
                resultingSubsidies, SubsidyCompensationOutputDTO::toInputForAnotherCompensationRound);
    }

    // Accessors -->

    public SubsidyAllocationCompensationBasis getBasis() {
        return basis;
    }

    public List<SubsidyCompensationOutputDTO> getResultingSubsidies() {
        return resultingSubsidies;
    }
}
