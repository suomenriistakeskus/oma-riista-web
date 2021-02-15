package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.requireNonNull;

// In stage 3, previously granted subsidies are added and lower limits imposed
// by subsidies granted last year are calculated. In later stages, RHYs whose
// subsidies are falling below calculated lower limits will be compensated at
// the expense of other RHYs.
public final class SubsidyAllocationStage3Calculation {

    public static List<RhySubsidyStage3DTO> addSubsidyComparisonToLastYear(
            @Nonnull final List<RhySubsidyStage2DTO> rhyAllocations,
            @Nonnull final PreviouslyGrantedSubsidiesDTO previouslyGrantedSubsidies) {

        requireNonNull(rhyAllocations);
        requireNonNull(previouslyGrantedSubsidies);

        return F.mapNonNullsToList(rhyAllocations, stage2Allocation -> {

            final String rhyCode = stage2Allocation.getRhyCode();

            final BigDecimal subsidyGrantedLastYear = previouslyGrantedSubsidies.getSubsidyGrantedLastYear(rhyCode);

            if (subsidyGrantedLastYear == null) {
                throw new NullPointerException("Could not find subsidy of last year for RHY with code: " + rhyCode);
            }

            final BigDecimal subsidyAfterStage2 =
                    stage2Allocation.getCalculation().getSubsidyAfterStage2RemainderAllocation();

            return new RhySubsidyStage3DTO(
                    stage2Allocation,
                    SubsidyComparisonToLastYearDTO.create(subsidyAfterStage2, subsidyGrantedLastYear));
        });
    }

    private SubsidyAllocationStage3Calculation() {
        throw new AssertionError();
    }
}
