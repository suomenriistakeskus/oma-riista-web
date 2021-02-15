package fi.riista.feature.organization.rhy.subsidy;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class RhySubsidyStage1DTO {

    private final RhyAndRkaDTO organisationInfo;
    private final SubsidyCalculationStage1DTO calculation;

    public RhySubsidyStage1DTO(@Nonnull final RhyAndRkaDTO organisationInfo,
                               @Nonnull final SubsidyCalculationStage1DTO calculation) {

        this.organisationInfo = requireNonNull(organisationInfo);
        this.calculation = requireNonNull(calculation);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public String getRhyCode() {
        return organisationInfo.getRhy().getOfficialCode();
    }

    // Accessors -->

    public RhyAndRkaDTO getOrganisationInfo() {
        return organisationInfo;
    }

    public SubsidyCalculationStage1DTO getCalculation() {
        return calculation;
    }
}
