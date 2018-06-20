package fi.riista.feature.permit.application.species;

import fi.riista.feature.gamediary.GameSpecies;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import java.util.Objects;

import static fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount.MAX_SPECIES_AMOUNT;
import static fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount.MIN_SPECIES_AMOUNT;

public class HarvestPermitApplicationSpeciesAmountDTO {

    @Nonnull
    public static HarvestPermitApplicationSpeciesAmountDTO create(
            @Nonnull final HarvestPermitApplicationSpeciesAmount speciesAmount) {
        return new HarvestPermitApplicationSpeciesAmountDTO(speciesAmount, speciesAmount.getGameSpecies());
    }

    public HarvestPermitApplicationSpeciesAmountDTO() {
    }

    public HarvestPermitApplicationSpeciesAmountDTO(
            @Nonnull final HarvestPermitApplicationSpeciesAmount speciesAmount,
            @Nonnull final GameSpecies species) {
        Objects.requireNonNull(speciesAmount, "speciesAmount must not be null");
        Objects.requireNonNull(species, "species must not be null");

        this.gameSpeciesCode = species.getOfficialCode();
        this.amount = speciesAmount.getAmount();
        this.description = speciesAmount.getDescription();
    }

    @AssertTrue
    public boolean isValidAmount() {
        return amount >= MIN_SPECIES_AMOUNT && amount < MAX_SPECIES_AMOUNT;
    }

    private int gameSpeciesCode;

    private float amount;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String description;

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
