package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyCompensationInputDTO;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class SubsidyAllocationStage3DTO {

    private final OrganisationNameDTO rhy;
    private final OrganisationNameDTO rka;

    private final StatisticsBasedSubsidyShareDTO calculatedShares;

    private final BigDecimal totalRoundedShare;

    // Even remainder euros allocated within rounding operations.
    private final int remainderEurosGivenInStage2;

    private final SubsidyBatchInfoDTO subsidyBatchInfo;

    public SubsidyAllocationStage3DTO(@Nonnull final OrganisationNameDTO rhy,
                                      @Nonnull final OrganisationNameDTO rka,
                                      @Nonnull final StatisticsBasedSubsidyShareDTO calculatedShares,
                                      @Nonnull final BigDecimal totalRoundedShare,
                                      final int remainderEurosGivenInStage2,
                                      @Nonnull final SubsidyBatchInfoDTO subsidyBatchInfo) {

        this.rhy = requireNonNull(rhy);
        this.rka = requireNonNull(rka);

        this.calculatedShares = requireNonNull(calculatedShares);
        this.totalRoundedShare = requireNonNull(totalRoundedShare);

        checkArgument(remainderEurosGivenInStage2 >= 0, "Remainder euros in stage 2 must not be negative");
        this.remainderEurosGivenInStage2 = remainderEurosGivenInStage2;

        this.subsidyBatchInfo = requireNonNull(subsidyBatchInfo);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public String getRhyCode() {
        return rhy.getOfficialCode();
    }

    public SubsidyCompensationInputDTO toCompensationInput() {
        return new SubsidyCompensationInputDTO(
                getRhyCode(),
                subsidyBatchInfo.calculateTotalSubsidyForCurrentYearBeforeCompensation(),
                subsidyBatchInfo.getSubsidyGrantedInFirstBatch(),
                subsidyBatchInfo.getSubsidyLowerLimitBasedOnLastYear(),
                false);
    }

    // Accessors -->

    public OrganisationNameDTO getRhy() {
        return rhy;
    }

    public OrganisationNameDTO getRka() {
        return rka;
    }

    public StatisticsBasedSubsidyShareDTO getCalculatedShares() {
        return calculatedShares;
    }

    public BigDecimal getTotalRoundedShare() {
        return totalRoundedShare;
    }

    public int getRemainderEurosGivenInStage2() {
        return remainderEurosGivenInStage2;
    }

    public SubsidyBatchInfoDTO getSubsidyBatchInfo() {
        return subsidyBatchInfo;
    }
}
