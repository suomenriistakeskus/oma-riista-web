package fi.riista.feature.organization.rhy.subsidy;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class RhySubsidyStage4DTO {

    private final RhyAndRkaDTO organisationInfo;
    private final SubsidyCalculationStage4DTO calculation;

    public RhySubsidyStage4DTO(@Nonnull final RhyAndRkaDTO organisationInfo,
                               @Nonnull final SubsidyCalculationStage4DTO calculation) {

        this.organisationInfo = requireNonNull(organisationInfo);
        this.calculation = requireNonNull(calculation);
    }

    public RhySubsidyStage4DTO(@Nonnull final RhySubsidyStage3DTO stage3Allocation,
                               @Nonnull final SubsidyRoundingDTO subsidyRounding) {

        this.organisationInfo = stage3Allocation.getOrganisationInfo();

        final SubsidyCalculationStage3DTO stage3Calculation = stage3Allocation.getCalculation();

        this.calculation = new SubsidyCalculationStage4DTO(
                stage3Calculation.getCalculatedShares(),
                stage3Calculation.getSubsidyAfterStage2RemainderAllocation(),
                stage3Calculation.getRemainderEurosGivenInStage2(),
                stage3Calculation.getSubsidyComparisonToLastYear(),
                subsidyRounding);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    // Accessors -->

    public RhyAndRkaDTO getOrganisationInfo() {
        return organisationInfo;
    }

    public SubsidyCalculationStage4DTO getCalculation() {
        return calculation;
    }
}
