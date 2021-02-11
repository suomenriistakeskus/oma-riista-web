package fi.riista.feature.organization.rhy.subsidy2019;

import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.subsidy.StatisticsBasedSubsidyShareDTO;
import fi.riista.feature.organization.rhy.subsidy.SubsidyRoundingDTO;
import fi.riista.util.F;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.NumberUtils.sum;
import static java.util.Objects.requireNonNull;

// Contains all data for subsidy allocation related to one organisation.
public class SubsidyAllocation2019Stage4DTO {

    private final OrganisationNameDTO organisation;
    private final OrganisationNameDTO parentOrganisation;

    private final StatisticsBasedSubsidyShareDTO calculatedShares;

    // Even remainder euros allocated within rounding operations.
    private final int remainderEurosGivenInStage2;

    private final SubsidyBatch2019InfoDTO subsidyBatchInfo;

    private final SubsidyRoundingDTO stage4Rounding;

    // Produces a summary over given iterable of allocations. All numeric amounts are added together.
    public static SubsidyAllocation2019Stage4DTO aggregate(@Nonnull final Iterable<SubsidyAllocation2019Stage4DTO> allocations,
                                                           @Nullable final OrganisationNameDTO targetOrganisation) {
        requireNonNull(allocations);

        final StatisticsBasedSubsidyShareDTO sumOfCalculatedShares = StatisticsBasedSubsidyShareDTO.aggregate(
                F.mapNonNullsToList(allocations, SubsidyAllocation2019Stage4DTO::getCalculatedShares));

        final int sumOfRemainderEurosInStage2 =
                sum(allocations, SubsidyAllocation2019Stage4DTO::getRemainderEurosGivenInStage2);

        final SubsidyBatch2019InfoDTO aggregateOfSubsidyBatchInfos = SubsidyBatch2019InfoDTO.aggregate(
                F.mapNonNullsToList(allocations, SubsidyAllocation2019Stage4DTO::getSubsidyBatchInfo));

        final SubsidyRoundingDTO aggregateOfSubsidyRoundings = SubsidyRoundingDTO.aggregate(
                F.mapNonNullsToList(allocations, SubsidyAllocation2019Stage4DTO::getStage4Rounding));

        return new SubsidyAllocation2019Stage4DTO(
                targetOrganisation,
                null,
                sumOfCalculatedShares,
                sumOfRemainderEurosInStage2,
                aggregateOfSubsidyBatchInfos,
                aggregateOfSubsidyRoundings);
    }

    public SubsidyAllocation2019Stage4DTO(@Nullable final OrganisationNameDTO organisation,
                                          @Nullable final OrganisationNameDTO parentOrganisation,
                                          @Nonnull final StatisticsBasedSubsidyShareDTO calculatedShares,
                                          final int remainderEurosGivenInStage2,
                                          @Nonnull final SubsidyBatch2019InfoDTO subsidyBatchInfo,
                                          @Nonnull final SubsidyRoundingDTO stage4Rounding) {

        this.organisation = organisation;
        this.parentOrganisation = parentOrganisation;

        this.calculatedShares = requireNonNull(calculatedShares);

        checkArgument(remainderEurosGivenInStage2 >= 0, "Remainder euros in stage 2 must not be negative");

        this.remainderEurosGivenInStage2 = remainderEurosGivenInStage2;

        this.subsidyBatchInfo = requireNonNull(subsidyBatchInfo);

        this.stage4Rounding = requireNonNull(stage4Rounding);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public BigDecimal calculateTotalSubsidyForCurrentYearBeforeFinalRounding() {
        return stage4Rounding.getSubsidyBeforeRounding().add(subsidyBatchInfo.getSubsidyGrantedInFirstBatch());
    }

    public BigDecimal calculateTotalSubsidyForCurrentYearAfterFinalRounding() {
        return stage4Rounding.getSubsidyAfterRounding().add(subsidyBatchInfo.getSubsidyGrantedInFirstBatch());
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

    public int getRemainderEurosGivenInStage2() {
        return remainderEurosGivenInStage2;
    }

    public SubsidyBatch2019InfoDTO getSubsidyBatchInfo() {
        return subsidyBatchInfo;
    }

    public SubsidyRoundingDTO getStage4Rounding() {
        return stage4Rounding;
    }
}
