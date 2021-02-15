package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.OrganisationNameDTO;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public class RhySubsidyStage5DTO {

    private final RhyAndRkaDTO organisationInfo;
    private final SubsidyCalculationStage5DTO calculation;

    public RhySubsidyStage5DTO(@Nonnull final RhyAndRkaDTO organisationInfo,
                               @Nonnull final SubsidyCalculationStage5DTO calculation) {

        this.organisationInfo = requireNonNull(organisationInfo);
        this.calculation = requireNonNull(calculation);
    }

    public RhySubsidyStage5DTO(@Nonnull final RhySubsidyStage4DTO stage4Allocation,
                               @Nonnull final BigDecimal subsidyOfBatch1,
                               @Nonnull final BigDecimal subsidyOfBatch2) {

        this.organisationInfo = stage4Allocation.getOrganisationInfo();

        final SubsidyCalculationStage4DTO stage4Calculation = stage4Allocation.getCalculation();

        this.calculation = new SubsidyCalculationStage5DTO(
                stage4Calculation.getCalculatedShares(),
                stage4Calculation.getSubsidyAfterStage2RemainderAllocation(),
                stage4Calculation.getRemainderEurosGivenInStage2(),
                stage4Calculation.getSubsidyComparisonToLastYear(),
                stage4Calculation.getStage4Rounding(),
                subsidyOfBatch1,
                subsidyOfBatch2);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public OrganisationNameDTO getRhy() {
        return organisationInfo.getRhy();
    }

    public OrganisationNameDTO getRka() {
        return organisationInfo.getRka();
    }

    // Accessors -->

    public RhyAndRkaDTO getOrganisationInfo() {
        return organisationInfo;
    }

    public SubsidyCalculationStage5DTO getCalculation() {
        return calculation;
    }
}
