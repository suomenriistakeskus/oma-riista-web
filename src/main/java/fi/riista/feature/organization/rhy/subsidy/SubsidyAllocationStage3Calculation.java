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

    public static List<SubsidyAllocationStage3DTO> addSubsidyBatchInfo(
            @Nonnull final List<BasicSubsidyAllocationDTO> rhyAllocations,
            @Nonnull final PreviouslyGrantedSubsidiesDTO previouslyGrantedSubsidies) {

        requireNonNull(rhyAllocations);
        requireNonNull(previouslyGrantedSubsidies);

        return F.mapNonNullsToList(rhyAllocations, allocation -> {

            final String rhyCode = allocation.getRhyCode();

            final BigDecimal subsidyCalculatedForSecondBatch = allocation.getTotalRoundedShare();

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

            final SubsidyBatchInfoDTO subsidyBatchInfo = SubsidyBatchInfoDTO.create(
                    subsidyCalculatedForSecondBatch, subsidyGrantedInFirstBatch, subsidyGrantedLastYear);

            return new SubsidyAllocationStage3DTO(
                    allocation.getRhy(),
                    allocation.getRka(),
                    allocation.getCalculatedShares(),
                    allocation.getTotalRoundedShare(),
                    allocation.getGivenRemainderEuros(),
                    subsidyBatchInfo);
        });
    }

    private SubsidyAllocationStage3Calculation() {
        throw new AssertionError();
    }
}
