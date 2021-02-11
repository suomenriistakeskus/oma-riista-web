package fi.riista.feature.permit.application;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Objects;

public class HarvestPermitApplicationSpeciesAmountDTO {

    private final GameSpeciesDTO gameSpecies;

    private final Float specimenAmount;
    private final Integer nestAmount;
    private final Integer eggAmount;
    private final Integer constructionAmount;

    private final String mooselikeDescription;

    private final LocalDate beginDate;
    private final LocalDate endDate;

    private final Integer validityYears;
    private final String additionalPeriodInfo;

    private final Integer causedDamageAmount;
    private final String causedDamageDescription;

    private final String evictionMeasureDescription;
    private final String evictionMeasureEffect;

    private final String populationAmount;
    private final String populationDescription;

    private final String forbiddenMethodJustification;
    private final Boolean forbiddenMethodsUsed;

    private final String subSpeciesName;

    public HarvestPermitApplicationSpeciesAmountDTO (final @Nonnull HarvestPermitApplicationSpeciesAmount speciesAmount,
                                                     final @Nonnull GameSpecies gameSpecies) {
        Objects.requireNonNull(speciesAmount);
        Objects.requireNonNull(gameSpecies);

        this.gameSpecies = GameSpeciesDTO.create(gameSpecies);

        this.specimenAmount = speciesAmount.getSpecimenAmount();
        this.nestAmount = speciesAmount.getNestAmount();
        this.eggAmount = speciesAmount.getEggAmount();
        this.constructionAmount = speciesAmount.getConstructionAmount();

        this.mooselikeDescription = speciesAmount.getMooselikeDescription();

        this.beginDate = speciesAmount.getBeginDate();
        this.endDate = speciesAmount.getEndDate();

        this.validityYears = speciesAmount.getValidityYears();
        this.additionalPeriodInfo = speciesAmount.getAdditionalPeriodInfo();

        this.causedDamageAmount = speciesAmount.getCausedDamageAmount();
        this.causedDamageDescription = speciesAmount.getCausedDamageDescription();

        this.evictionMeasureDescription = speciesAmount.getEvictionMeasureDescription();
        this.evictionMeasureEffect = speciesAmount.getEvictionMeasureEffect();

        this.populationAmount = speciesAmount.getPopulationAmount();
        this.populationDescription = speciesAmount.getPopulationDescription();

        this.forbiddenMethodJustification = speciesAmount.getForbiddenMethodJustification();
        this.forbiddenMethodsUsed = speciesAmount.isForbiddenMethodsUsed();

        this.subSpeciesName = speciesAmount.getSubSpeciesName();
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

    public String getMooselikeDescription() {
        return mooselikeDescription;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Integer getValidityYears() {
        return validityYears;
    }

    public String getAdditionalPeriodInfo() {
        return additionalPeriodInfo;
    }

    public Integer getCausedDamageAmount() {
        return causedDamageAmount;
    }

    public String getCausedDamageDescription() {
        return causedDamageDescription;
    }

    public String getEvictionMeasureDescription() {
        return evictionMeasureDescription;
    }

    public String getEvictionMeasureEffect() {
        return evictionMeasureEffect;
    }

    public String getPopulationAmount() {
        return populationAmount;
    }

    public String getPopulationDescription() {
        return populationDescription;
    }

    public String getForbiddenMethodJustification() {
        return forbiddenMethodJustification;
    }

    public Boolean isForbiddenMethodsUsed() {
        return forbiddenMethodsUsed;
    }

    public String getSubSpeciesName() {
        return subSpeciesName;
    }
}
