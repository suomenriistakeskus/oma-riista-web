package fi.riista.feature.permit.application.research.amount;

import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;

import static java.util.Objects.requireNonNull;

public class ResearchSpeciesAmountDTO {
    private static final int MAX_SPECIES_AMOUNT = 100_000;
    private static final int MIN_SPECIES_AMOUNT = 1;

    public ResearchSpeciesAmountDTO() {
    }

    public ResearchSpeciesAmountDTO(final @Nonnull HarvestPermitApplicationSpeciesAmount speciesAmount) {
        requireNonNull(speciesAmount, "speciesAmount must not be null");
        requireNonNull(speciesAmount.getGameSpecies(), "species must not be null");

        this.gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        this.amount = speciesAmount.getSpecimenAmount().intValue();
    }

    @AssertTrue
    public boolean isValidAmount() {
        return amount >= MIN_SPECIES_AMOUNT && amount < MAX_SPECIES_AMOUNT;
    }

    private int gameSpeciesCode;

    private int amount;

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(final int amount) {
        this.amount = amount;
    }

}
