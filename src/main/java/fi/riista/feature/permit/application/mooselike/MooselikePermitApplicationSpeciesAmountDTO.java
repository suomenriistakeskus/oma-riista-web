package fi.riista.feature.permit.application.mooselike;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import java.util.Objects;

public class MooselikePermitApplicationSpeciesAmountDTO {
    private static final int MAX_SPECIES_AMOUNT = 10_000;
    private static final int MIN_SPECIES_AMOUNT = 1;

    @Nonnull
    public static MooselikePermitApplicationSpeciesAmountDTO create(
            @Nonnull final HarvestPermitApplicationSpeciesAmount speciesAmount) {
        return new MooselikePermitApplicationSpeciesAmountDTO(speciesAmount, speciesAmount.getGameSpecies());
    }

    public MooselikePermitApplicationSpeciesAmountDTO() {
    }

    public MooselikePermitApplicationSpeciesAmountDTO(
            @Nonnull final HarvestPermitApplicationSpeciesAmount speciesAmount,
            @Nonnull final GameSpecies species) {
        Objects.requireNonNull(speciesAmount, "speciesAmount must not be null");
        Objects.requireNonNull(species, "species must not be null");

        this.gameSpeciesCode = species.getOfficialCode();
        this.amount = speciesAmount.getAmount();
        this.description = speciesAmount.getMooselikeDescription();
    }

    @AssertTrue
    public boolean isValidAmount() {
        return amount >= MIN_SPECIES_AMOUNT && amount < MAX_SPECIES_AMOUNT;
    }

    @AssertTrue
    public boolean isValidSpecies() {
        return GameSpecies.isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode);
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
