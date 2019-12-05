package fi.riista.feature.permit.application.mammal.period;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.Optional;

import static fi.riista.feature.permit.application.mammal.period.MammalPermitApplicationPeriodRestrictions.getSpeciesPeriodRestriction;
import static java.util.Objects.requireNonNull;

public class MammalPermitApplicationSpeciesPeriodDTO implements HasBeginAndEndDate {


    public MammalPermitApplicationSpeciesPeriodDTO() {
    }

    public MammalPermitApplicationSpeciesPeriodDTO(final @Nonnull HarvestPermitApplicationSpeciesAmount speciesAmount) {
        requireNonNull(speciesAmount, "speciesAmount must not be null");
        requireNonNull(speciesAmount.getGameSpecies(), "species must not be null");

        this.gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        this.beginDate = speciesAmount.getBeginDate();
        this.endDate = speciesAmount.getEndDate();
        this.additionalPeriodInfo = speciesAmount.getAdditionalPeriodInfo();
        this.maxPeriod = getSpeciesPeriodRestriction(this.gameSpeciesCode);
    }

    private int gameSpeciesCode;

    @NotNull
    private LocalDate beginDate;

    @NotNull
    private LocalDate endDate;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String additionalPeriodInfo;

    private Integer maxPeriod;

    @AssertTrue
    public boolean isValidPeriod() {
        return this.beginDate != null && this.endDate != null &&
                periodLengthInBounds();
    }

    private boolean periodLengthInBounds() {
        final LocalDate maxEnd = Optional.ofNullable(getSpeciesPeriodRestriction(gameSpeciesCode))
                .map(beginDate::plusDays)
                .orElse(beginDate.plusYears(1).minusDays(1));
        return !endDate.isAfter(maxEnd);
    }

    public boolean isRestricted() {
        return MammalPermitApplicationPeriodRestrictions.isRestricted(this.gameSpeciesCode);
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

    public Integer getMaxPeriod() {
        return maxPeriod;
    }

    public void setMaxPeriod(final Integer maxPeriod) {
        this.maxPeriod = maxPeriod;
    }
}
