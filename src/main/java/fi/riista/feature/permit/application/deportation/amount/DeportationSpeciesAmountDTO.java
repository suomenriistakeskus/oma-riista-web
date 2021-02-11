package fi.riista.feature.permit.application.deportation.amount;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

public class DeportationSpeciesAmountDTO {
    private static final int MAX_SPECIES_AMOUNT = 100_000;
    private static final int MIN_SPECIES_AMOUNT = 1;

    @AssertTrue
    public boolean isValidAmount() {
        return amount >= MIN_SPECIES_AMOUNT && amount < MAX_SPECIES_AMOUNT;
    }

    @NotNull
    private Integer gameSpeciesCode;

    @NotNull
    private Integer amount;

    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(final Integer amount) {
        this.amount = amount;
    }

}
