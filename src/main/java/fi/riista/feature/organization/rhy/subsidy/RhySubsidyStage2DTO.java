package fi.riista.feature.organization.rhy.subsidy;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public class RhySubsidyStage2DTO {

    private final RhyAndRkaDTO organisationInfo;
    private final SubsidyCalculationStage2DTO calculation;

    public RhySubsidyStage2DTO(@Nonnull final RhyAndRkaDTO organisationInfo,
                               @Nonnull final SubsidyCalculationStage2DTO calculation) {

        this.organisationInfo = requireNonNull(organisationInfo);
        this.calculation = requireNonNull(calculation);
    }

    public RhySubsidyStage2DTO(@Nonnull final RhySubsidyStage1DTO stage1Allocation,
                               final int remainderEurosGivenInStage2) {

        this.organisationInfo = stage1Allocation.getOrganisationInfo();

        final SubsidyCalculationStage1DTO stage1Calculation = stage1Allocation.getCalculation();

        this.calculation = new SubsidyCalculationStage2DTO(
                stage1Calculation.getCalculatedShares(),
                stage1Calculation.getTotalRoundedShare().add(new BigDecimal(remainderEurosGivenInStage2)),
                remainderEurosGivenInStage2);
    }

    public String getRhyCode() {
        return organisationInfo.getRhy().getOfficialCode();
    }

    // Accessors -->

    public RhyAndRkaDTO getOrganisationInfo() {
        return organisationInfo;
    }

    public SubsidyCalculationStage2DTO getCalculation() {
        return calculation;
    }
}
