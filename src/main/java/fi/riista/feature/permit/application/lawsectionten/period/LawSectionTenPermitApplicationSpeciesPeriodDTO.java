package fi.riista.feature.permit.application.lawsectionten.period;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.validation.DoNotValidate;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import static fi.riista.feature.permit.application.lawsectionten.period.LawSectionTenPermitApplicationSpeciesPeriodSeasons.getPermitSeasons;

public class LawSectionTenPermitApplicationSpeciesPeriodDTO implements HasBeginAndEndDate {

    public LawSectionTenPermitApplicationSpeciesPeriodDTO() {}

    public LawSectionTenPermitApplicationSpeciesPeriodDTO(final @Nonnull HarvestPermitApplicationSpeciesAmount speciesAmount) {
        this.gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        this.beginDate = speciesAmount.getBeginDate();
        this.endDate = speciesAmount.getEndDate();
        this.seasons = getPermitSeasons(speciesAmount.getGameSpecies().getOfficialCode());
    }

    private int gameSpeciesCode;

    @NotNull
    private LocalDate beginDate;

    @NotNull
    private LocalDate endDate;

    @DoNotValidate
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LawSectionTenPermitApplicationSpeciesPeriodSeasons seasons;

    @AssertTrue
    public boolean isValidPeriod() {
        return this.beginDate != null && this.endDate != null;
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

    public LawSectionTenPermitApplicationSpeciesPeriodSeasons getSeasons() {
        return seasons;
    }
}
