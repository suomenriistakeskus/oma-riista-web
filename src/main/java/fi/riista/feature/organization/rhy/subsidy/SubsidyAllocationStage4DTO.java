package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.F;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.NumberUtils.nullableSum;
import static fi.riista.util.NumberUtils.sum;
import static java.util.Objects.requireNonNull;

// Contains all data for subsidy allocation related to one organisation.
public class SubsidyAllocationStage4DTO {

    private final OrganisationNameDTO organisation;
    private final OrganisationNameDTO parentOrganisation;

    private final StatisticsBasedSubsidyShareDTO calculatedShares;

    private final BigDecimal calculatedSubsidyBeforeFinalRounding;
    private final BigDecimal calculatedSubsidyAfterFinalRounding;

    private final SubsidyBatchInfoDTO subsidyBatchInfo;

    // Even remainder euros allocated within rounding operations.
    private final int remainderEurosGivenInStage2;
    private final int remainderEurosGivenInStage4;

    // Produces a summary over given iterable of allocations. All numeric amounts are added together.
    public static SubsidyAllocationStage4DTO aggregate(@Nonnull final Iterable<SubsidyAllocationStage4DTO> allocations,
                                                       @Nullable final OrganisationNameDTO targetOrganisation) {
        requireNonNull(allocations);

        final StatisticsBasedSubsidyShareDTO sumOfCalculatedShares = StatisticsBasedSubsidyShareDTO.aggregate(
                F.mapNonNullsToList(allocations, SubsidyAllocationStage4DTO::getCalculatedShares));

        final BigDecimal sumCalculatedSubsidiesBeforeFinalRounding =
                nullableSum(allocations, SubsidyAllocationStage4DTO::getCalculatedSubsidyBeforeFinalRounding);

        final BigDecimal sumCalculatedSubsidiesAfterFinalRounding =
                nullableSum(allocations, SubsidyAllocationStage4DTO::getCalculatedSubsidyAfterFinalRounding);

        final SubsidyBatchInfoDTO aggregateOfSubsidyBatchInfos = SubsidyBatchInfoDTO.aggregate(
                F.mapNonNullsToList(allocations, SubsidyAllocationStage4DTO::getSubsidyBatchInfo));

        final int sumOfRemainderEurosInStage2 =
                sum(allocations, SubsidyAllocationStage4DTO::getRemainderEurosGivenInStage2);

        final int sumOfRemainderEurosInStage4 =
                sum(allocations, SubsidyAllocationStage4DTO::getRemainderEurosGivenInStage4);

        return new SubsidyAllocationStage4DTO(
                targetOrganisation,
                null,
                sumOfCalculatedShares,
                sumCalculatedSubsidiesBeforeFinalRounding,
                sumCalculatedSubsidiesAfterFinalRounding,
                aggregateOfSubsidyBatchInfos,
                sumOfRemainderEurosInStage2,
                sumOfRemainderEurosInStage4);
    }

    public SubsidyAllocationStage4DTO(@Nullable final OrganisationNameDTO organisation,
                                      @Nullable final OrganisationNameDTO parentOrganisation,
                                      @Nonnull final StatisticsBasedSubsidyShareDTO calculatedShares,
                                      @Nonnull final BigDecimal calculatedSubsidyBeforeFinalRounding,
                                      @Nonnull final BigDecimal calculatedSubsidyAfterFinalRounding,
                                      @Nonnull final SubsidyBatchInfoDTO subsidyBatchInfo,
                                      final int remainderEurosGivenInStage2,
                                      final int remainderEurosGivenInStage4) {

        this.organisation = organisation;
        this.parentOrganisation = parentOrganisation;

        this.calculatedShares = requireNonNull(calculatedShares);

        this.calculatedSubsidyBeforeFinalRounding = requireNonNull(calculatedSubsidyBeforeFinalRounding);
        this.calculatedSubsidyAfterFinalRounding = requireNonNull(calculatedSubsidyAfterFinalRounding);

        this.subsidyBatchInfo = requireNonNull(subsidyBatchInfo);

        checkArgument(remainderEurosGivenInStage2 >= 0, "Remainder euros in stage 2 must not be negative");
        checkArgument(remainderEurosGivenInStage4 >= 0, "Remainder euros in stage 4 must not be negative");

        this.remainderEurosGivenInStage2 = remainderEurosGivenInStage2;
        this.remainderEurosGivenInStage4 = remainderEurosGivenInStage4;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public BigDecimal calculateTotalSubsidyForCurrentYearBeforeFinalRounding() {
        return calculatedSubsidyBeforeFinalRounding.add(subsidyBatchInfo.getSubsidyGrantedInFirstBatch());
    }

    public BigDecimal calculateTotalSubsidyForCurrentYearAfterFinalRounding() {
        return calculatedSubsidyAfterFinalRounding.add(subsidyBatchInfo.getSubsidyGrantedInFirstBatch());
    }

    // Accessors -->

    public OrganisationNameDTO getOrganisation() {
        return organisation;
    }

    public OrganisationNameDTO getParentOrganisation() {
        return parentOrganisation;
    }

    public StatisticsBasedSubsidyShareDTO getCalculatedShares() {
        return calculatedShares;
    }

    public BigDecimal getCalculatedSubsidyBeforeFinalRounding() {
        return calculatedSubsidyBeforeFinalRounding;
    }

    public BigDecimal getCalculatedSubsidyAfterFinalRounding() {
        return calculatedSubsidyAfterFinalRounding;
    }

    public SubsidyBatchInfoDTO getSubsidyBatchInfo() {
        return subsidyBatchInfo;
    }

    public int getRemainderEurosGivenInStage2() {
        return remainderEurosGivenInStage2;
    }

    public int getRemainderEurosGivenInStage4() {
        return remainderEurosGivenInStage4;
    }
}
