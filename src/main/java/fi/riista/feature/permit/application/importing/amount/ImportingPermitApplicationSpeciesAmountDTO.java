package fi.riista.feature.permit.application.importing.amount;

import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import static fi.riista.util.F.mapNullable;
import static java.util.Objects.requireNonNull;

public class ImportingPermitApplicationSpeciesAmountDTO {
    private static final int MAX_AMOUNT_VALUE = 100_000;
    private static final int MIN_AMOUNT_VALUE = 1;

    public ImportingPermitApplicationSpeciesAmountDTO() {}

    public ImportingPermitApplicationSpeciesAmountDTO(final @Nonnull HarvestPermitApplicationSpeciesAmount speciesAmount) {
        requireNonNull(speciesAmount, "speciesAmount must not be null");
        requireNonNull(speciesAmount.getGameSpecies(), "species must not be null");

        this.gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        this.specimenAmount = mapNullable(speciesAmount.getSpecimenAmount(), Float::intValue);
        this.eggAmount = speciesAmount.getEggAmount();
        this.subSpeciesName = speciesAmount.getSubSpeciesName();
    }

    private int gameSpeciesCode;

    @Min(MIN_AMOUNT_VALUE)
    @Max(MAX_AMOUNT_VALUE)
    private Integer specimenAmount;

    @Min(MIN_AMOUNT_VALUE)
    @Max(MAX_AMOUNT_VALUE)
    private Integer eggAmount;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String subSpeciesName;

    @AssertTrue
    public boolean isAmountPresent() {
        return F.anyNonNull(specimenAmount, eggAmount);
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
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
