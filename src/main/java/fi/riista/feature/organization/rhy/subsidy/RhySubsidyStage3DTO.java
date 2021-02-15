package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyCompensationInputDTO;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class RhySubsidyStage3DTO {

    private final RhyAndRkaDTO organisationInfo;
    private final SubsidyCalculationStage3DTO calculation;

    public RhySubsidyStage3DTO(@Nonnull final RhyAndRkaDTO organisationInfo,
                               @Nonnull final SubsidyCalculationStage3DTO calculation) {

        this.organisationInfo = requireNonNull(organisationInfo);
        this.calculation = requireNonNull(calculation);
    }

    public RhySubsidyStage3DTO(@Nonnull final RhySubsidyStage2DTO stage2Allocation,
                               @Nonnull final SubsidyComparisonToLastYearDTO subsidyComparisonToLastYear) {

        this.organisationInfo = stage2Allocation.getOrganisationInfo();

        final SubsidyCalculationStage2DTO stage2Calculation = stage2Allocation.getCalculation();

        this.calculation = new SubsidyCalculationStage3DTO(
                stage2Calculation.getCalculatedShares(),
                stage2Calculation.getSubsidyAfterStage2RemainderAllocation(),
                stage2Calculation.getRemainderEurosGivenInStage2(),
                subsidyComparisonToLastYear);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public String getRhyCode() {
        return organisationInfo.getRhy().getOfficialCode();
    }

    public SubsidyCompensationInputDTO toCompensationInput() {
        return new SubsidyCompensationInputDTO(
                getRhyCode(),
                calculation.getSubsidyComparisonToLastYear().getSubsidyCalculatedBasedOnStatistics(),
                calculation.getSubsidyComparisonToLastYear().getSubsidyLowerLimitBasedOnLastYear(),
                false);
    }

    // Accessors -->

    public RhyAndRkaDTO getOrganisationInfo() {
        return organisationInfo;
    }

    public SubsidyCalculationStage3DTO getCalculation() {
        return calculation;
    }
}
