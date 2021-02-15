package fi.riista.feature.permit.application.gamemanagement.period;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

public class GameManagementSpeciesPeriodDTO implements HasBeginAndEndDate {

    private int gameSpeciesCode;

    @NotNull
    private LocalDate beginDate;

    @NotNull
    private LocalDate endDate;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String additionalPeriodInfo;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer validityYears;

    public GameManagementSpeciesPeriodDTO() {
    }

    public GameManagementSpeciesPeriodDTO(final @Nonnull HarvestPermitApplicationSpeciesAmount speciesAmount) {
        requireNonNull(speciesAmount, "speciesAmount must not be null");
        requireNonNull(speciesAmount.getGameSpecies(), "species must not be null");

        this.gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        this.beginDate = speciesAmount.getBeginDate();
        this.endDate = speciesAmount.getEndDate();
        this.additionalPeriodInfo = speciesAmount.getAdditionalPeriodInfo();
        this.validityYears = speciesAmount.getValidityYears();
    }

    @AssertTrue
    public boolean isValidPeriod() {
        final LocalDate maxEndDate = beginDate.plusYears(1).minusDays(1);
        return !endDate.isAfter(maxEndDate);
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getAdditionalPeriodInfo() {
        return additionalPeriodInfo;
    }

    public void setAdditionalPeriodInfo(final String additionalPeriodInfo) {
        this.additionalPeriodInfo = additionalPeriodInfo;
    }

    public Integer getValidityYears() {
        return validityYears;
    }

    public void setValidityYears(final Integer validityYears) {
        this.validityYears = validityYears;
    }
}
