package fi.riista.feature.permit.application.importing.period;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

public class ImportingPermitApplicationSpeciesPeriodDTO implements HasBeginAndEndDate {


    public ImportingPermitApplicationSpeciesPeriodDTO() {
    }

    public ImportingPermitApplicationSpeciesPeriodDTO(final @Nonnull HarvestPermitApplicationSpeciesAmount speciesAmount) {
        requireNonNull(speciesAmount, "speciesAmount must not be null");
        requireNonNull(speciesAmount.getGameSpecies(), "species must not be null");

        this.gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        this.beginDate = speciesAmount.getBeginDate();
        this.endDate = speciesAmount.getEndDate();
        this.additionalPeriodInfo = speciesAmount.getAdditionalPeriodInfo();
    }

    private int gameSpeciesCode;

    @NotNull
    private LocalDate beginDate;

    @NotNull
    private LocalDate endDate;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String additionalPeriodInfo;

    @AssertTrue
    public boolean isPeriodValid() {
        // End date should not be before begin and period should be one year at most
        return !this.beginDate.isAfter(this.endDate)
                && this.beginDate.plusYears(1).isAfter(this.endDate);
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
}
