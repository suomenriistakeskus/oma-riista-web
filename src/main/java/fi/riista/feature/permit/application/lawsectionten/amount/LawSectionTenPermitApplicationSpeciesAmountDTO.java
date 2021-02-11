package fi.riista.feature.permit.application.lawsectionten.amount;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;

import static java.util.Objects.requireNonNull;

public class LawSectionTenPermitApplicationSpeciesAmountDTO {
    private static final int MAX_SPECIES_AMOUNT = 100_000;
    private static final int MIN_SPECIES_AMOUNT = 1;

    public LawSectionTenPermitApplicationSpeciesAmountDTO() {}

    public LawSectionTenPermitApplicationSpeciesAmountDTO(final @Nonnull HarvestPermitApplicationSpeciesAmount speciesAmount) {
        requireNonNull(speciesAmount, "speciesAmount must not be null");
        requireNonNull(speciesAmount.getGameSpecies(), "species must not be null");

        this.gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        this.amount = speciesAmount.getSpecimenAmount();
    }

    @AssertTrue
    public boolean isValidAmount() {
        return amount >= MIN_SPECIES_AMOUNT && amount < MAX_SPECIES_AMOUNT;
    }

    @AssertTrue
    public boolean isValidSpecies() {
        return GameSpecies.isLawSectionTenPermitSpecies(gameSpeciesCode);
    }

    private int gameSpeciesCode;

    private float amount;

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(final float amount) {
        this.amount = amount;
    }
}
