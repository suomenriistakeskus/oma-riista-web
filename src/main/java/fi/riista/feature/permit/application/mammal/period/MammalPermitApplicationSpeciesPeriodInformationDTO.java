package fi.riista.feature.permit.application.mammal.period;

import fi.riista.feature.permit.application.mammal.MammalPermitApplication;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;


public class MammalPermitApplicationSpeciesPeriodInformationDTO {

    public MammalPermitApplicationSpeciesPeriodInformationDTO() {

    }

    public MammalPermitApplicationSpeciesPeriodInformationDTO(final List<MammalPermitApplicationSpeciesPeriodDTO> speciesPeriods,
                                                              final Integer validityYears,
                                                              final MammalPermitApplication.ExtendedPeriodGrounds grounds,
                                                              final String extendedPeriodGroundsDescription,
                                                              final String protectedAreaName) {
        this.speciesPeriods = speciesPeriods;
        this.validityYears = validityYears;
        this.protectedAreaName = protectedAreaName;
        this.extendedPeriodNotApplicable = containsRestrictedSpecies();
        this.extendedPeriodGrounds = grounds;
        this.extendedPeriodGroundsDescription = extendedPeriodGroundsDescription;
    }

    @Valid
    private List<MammalPermitApplicationSpeciesPeriodDTO> speciesPeriods;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer validityYears;

    private boolean extendedPeriodNotApplicable;

    private MammalPermitApplication.ExtendedPeriodGrounds extendedPeriodGrounds;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String extendedPeriodGroundsDescription;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String protectedAreaName;

    @AssertTrue
    public boolean isOneYearOrDoesNotContainRestrictedSpecies() {
        return validityYears == 1 || !containsRestrictedSpecies();
    }

    @AssertTrue
    public boolean isOneYearOrHasExtendedPeriodGrounds() {
        return validityYears == 1 ||
                (extendedPeriodGrounds != null &&
                        protectedAreaName != null);
    }

    private boolean containsRestrictedSpecies() {
        return speciesPeriods.stream()
                .filter(MammalPermitApplicationSpeciesPeriodDTO::isRestricted)
                .findAny()
                .isPresent();
    }

    public List<MammalPermitApplicationSpeciesPeriodDTO> getSpeciesPeriods() {
        return speciesPeriods;
    }

    public Integer getValidityYears() {
        return validityYears;
    }

    public void setValidityYears(final Integer validityYears) {
        this.validityYears = validityYears;
    }

    public boolean isExtendedPeriodNotApplicable() {
        return extendedPeriodNotApplicable;
    }

    public void setExtendedPeriodNotApplicable(final boolean extendedPeriodNotApplicable) {
        this.extendedPeriodNotApplicable = extendedPeriodNotApplicable;
    }

    public MammalPermitApplication.ExtendedPeriodGrounds getExtendedPeriodGrounds() {
        return extendedPeriodGrounds;
    }

    public void setExtendedPeriodGrounds(final MammalPermitApplication.ExtendedPeriodGrounds extendedPeriodGrounds) {
        this.extendedPeriodGrounds = extendedPeriodGrounds;
    }

    public String getExtendedPeriodGroundsDescription() {
        return extendedPeriodGroundsDescription;
    }

    public void setExtendedPeriodGroundsDescription(final String extendedPeriodGroundsDescription) {
        this.extendedPeriodGroundsDescription = extendedPeriodGroundsDescription;
    }

    public String getProtectedAreaName() {
        return protectedAreaName;
    }

    public void setProtectedAreaName(final String protectedAreaName) {
        this.protectedAreaName = protectedAreaName;
    }
}
