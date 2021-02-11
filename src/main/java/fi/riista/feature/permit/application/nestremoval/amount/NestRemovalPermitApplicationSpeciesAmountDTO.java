package fi.riista.feature.permit.application.nestremoval.amount;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class NestRemovalPermitApplicationSpeciesAmountDTO {
    private static final int MAX_SPECIES_AMOUNT = 100_000;
    private static final int MIN_SPECIES_AMOUNT = 1;

    public NestRemovalPermitApplicationSpeciesAmountDTO() {}

    public NestRemovalPermitApplicationSpeciesAmountDTO(final @Nonnull HarvestPermitApplicationSpeciesAmount speciesAmount) {
        requireNonNull(speciesAmount, "speciesAmount must not be null");
        requireNonNull(speciesAmount.getGameSpecies(), "species must not be null");

        this.gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        this.nestAmount = speciesAmount.getNestAmount();
        this.eggAmount = speciesAmount.getEggAmount();
        this.constructionAmount = speciesAmount.getConstructionAmount();
    }

    @AssertTrue
    public boolean isValidAmount() {
        final List<Integer> amounts = Stream.of(nestAmount, eggAmount, constructionAmount)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return !amounts.isEmpty() &&
                amounts.stream().allMatch(amount -> amount >= MIN_SPECIES_AMOUNT && amount < MAX_SPECIES_AMOUNT);
    }

    @AssertTrue
    public boolean isValidSpecies() {
        return GameSpecies.isNestRemovalPermitSpecies(gameSpeciesCode);
    }

    private int gameSpeciesCode;

    private Integer nestAmount;
    private Integer eggAmount;
    private Integer constructionAmount;


    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public Integer getNestAmount() {
        return nestAmount;
    }

    public void setNestAmount(final Integer nestAmount) {
        this.nestAmount = nestAmount;
    }

    public Integer getEggAmount() {
        return eggAmount;
    }

    public void setEggAmount(final Integer eggAmount) {
        this.eggAmount = eggAmount;
    }

    public Integer getConstructionAmount() {
        return constructionAmount;
    }

    public void setConstructionAmount(final Integer constructionAmount) {
        this.constructionAmount = constructionAmount;
    }
}
