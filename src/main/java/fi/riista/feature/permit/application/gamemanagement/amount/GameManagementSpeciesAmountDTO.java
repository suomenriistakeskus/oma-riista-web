package fi.riista.feature.permit.application.gamemanagement.amount;

import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class GameManagementSpeciesAmountDTO {
    private static final int MAX_SPECIES_AMOUNT = 99_999;
    private static final int MIN_SPECIES_AMOUNT = 1;

    private Integer gameSpeciesCode;

    @Min(MIN_SPECIES_AMOUNT)
    @Max(MAX_SPECIES_AMOUNT)
    private Integer specimenAmount;

    @Min(MIN_SPECIES_AMOUNT)
    @Max(MAX_SPECIES_AMOUNT)
    private Integer eggAmount;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String subSpeciesName;

    @AssertTrue
    public boolean isAmountPresent() {
        return F.anyNonNull(specimenAmount, eggAmount);
    }

    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public Integer getSpecimenAmount() {
        return specimenAmount;
    }

    public void setSpecimenAmount(final Integer specimenAmount) {
        this.specimenAmount = specimenAmount;
    }

    public Integer getEggAmount() {
        return eggAmount;
    }

    public void setEggAmount(final Integer eggAmount) {
        this.eggAmount = eggAmount;
    }

    public String getSubSpeciesName() {
        return subSpeciesName;
    }

    public void setSubSpeciesName(final String subSpeciesName) {
        this.subSpeciesName = subSpeciesName;
    }
}
