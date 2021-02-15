package fi.riista.feature.permit.application.search.excel;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Objects;

import static fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount.RestrictionType;

public class ApplicationSearchDecisionSpeciesAmountDTO {

    private final GameSpeciesDTO gameSpecies;

    private final Float specimenAmount;
    private final Integer nestAmount;
    private final Integer eggAmount;
    private final Integer constructionAmount;

    private final LocalDate beginDate;
    private final LocalDate endDate;

    private final LocalDate beginDate2;
    private final LocalDate endDate2;

    private final RestrictionType restrictionType;
    private final Float restrictionAmount;

    private boolean amountComplete;

    private boolean forbiddenMethodComplete;

    public ApplicationSearchDecisionSpeciesAmountDTO(final @Nonnull PermitDecisionSpeciesAmount speciesAmount,
                                                     final @Nonnull GameSpecies gameSpecies) {
        Objects.requireNonNull(speciesAmount);
        Objects.requireNonNull(gameSpecies);

        this.gameSpecies = GameSpeciesDTO.create(gameSpecies);

        this.specimenAmount = speciesAmount.getSpecimenAmount();
        this.nestAmount = speciesAmount.getNestAmount();
        this.eggAmount = speciesAmount.getNestAmount();
        this.constructionAmount = speciesAmount.getConstructionAmount();

        this.beginDate = speciesAmount.getBeginDate();
        this.endDate = speciesAmount.getEndDate();

        this.beginDate2 = speciesAmount.getBeginDate2();
        this.endDate2 = speciesAmount.getEndDate2();

        this.restrictionType = speciesAmount.getRestrictionType();
        this.restrictionAmount = speciesAmount.getRestrictionAmount();

        this.amountComplete = speciesAmount.isAmountComplete();

        this.forbiddenMethodComplete = speciesAmount.isForbiddenMethodComplete();
    }

    public GameSpeciesDTO getGameSpecies() {
        return gameSpecies;
    }

    public Float getSpecimenAmount() {
        return specimenAmount;
    }

    public Integer getNestAmount() {
        return nestAmount;
    }

    public Integer getEggAmount() {
        return eggAmount;
    }

    public Integer getConstructionAmount() {
        return constructionAmount;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getBeginDate2() {
        return beginDate2;
    }

    public LocalDate getEndDate2() {
        return endDate2;
    }

    public RestrictionType getRestrictionType() {
        return restrictionType;
    }

    public Float getRestrictionAmount() {
        return restrictionAmount;
    }

    public boolean isAmountComplete() {
        return amountComplete;
    }

    public boolean isForbiddenMethodComplete() {
        return forbiddenMethodComplete;
    }
}
