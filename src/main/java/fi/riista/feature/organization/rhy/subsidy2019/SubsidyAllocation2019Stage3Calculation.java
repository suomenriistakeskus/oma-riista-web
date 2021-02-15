package fi.riista.feature.organization.rhy.subsidy2019;

import fi.riista.feature.organization.rhy.subsidy.PreviouslyGrantedSubsidiesDTO;
import fi.riista.feature.organization.rhy.subsidy.RhySubsidyStage2DTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyCalculationStage2DTO;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.requireNonNull;

// In stage 3, previously granted subsidies are added and lower limits imposed
// by subsidies granted last year are calculated. In later stages, RHYs whose
// subsidies are falling below calculated lower limits will be compensated at
// the expense of other RHYs.
public final class SubsidyAllocation2019Stage3Calculation {

    public static List<SubsidyAllocation2019Stage3DTO> addSubsidyBatchInfo(
            @Nonnull final List<RhySubsidyStage2DTO> rhyAllocations,
            @Nonnull final PreviouslyGrantedSubsidiesDTO previouslyGrantedSubsidies) {

        requireNonNull(rhyAllocations);
        requireNonNull(previouslyGrantedSubsidies);

        return F.mapNonNullsToList(rhyAllocations, allocation -> {

            final String rhyCode = allocation.getRhyCode();

            final BigDecimal subsidyGrantedInFirstBatch =
                    previouslyGrantedSubsidies.getSubsidyGrantedInFirstBatchOfCurrentYear(rhyCode);

            if (subsidyGrantedInFirstBatch == null) {
                throw new NullPointerException(
                        "Could not find subsidy granted in first batch for RHY with code: " + rhyCode);
            }

            final BigDecimal subsidyGrantedLastYear = previouslyGrantedSubsidies.getSubsidyGrantedLastYear(rhyCode);

            if (subsidyGrantedLastYear == null) {
                throw new NullPointerException("Could not find subsidy of last year for RHY with code: " + rhyCode);
            }

            final SubsidyCalculationStage2DTO stage2Calculation = allocation.getCalculation();

            final BigDecimal subsidyCalculatedForSecondBatch =
                    stage2Calculation.getSubsidyAfterStage2RemainderAllocation();

            final SubsidyBatch2019InfoDTO subsidyBatchInfo = SubsidyBatch2019InfoDTO.create(
                    subsidyCalculatedForSecondBatch, subsidyGrantedInFirstBatch, subsidyGrantedLastYear);

            return new SubsidyAllocation2019Stage3DTO(
                    allocation.getOrganisationInfo().getRhy(),
                    allocation.getOrganisationInfo().getRka(),
                    stage2Calculation.getCalculatedShares(),
                    subsidyCalculatedForSecondBatch,
                    stage2Calculation.getRemainderEurosGivenInStage2(),
                    subsidyBatchInfo);
        });
    }

    private SubsidyAllocation2019Stage3Calculation() {
        throw new AssertionError();
    }
}
