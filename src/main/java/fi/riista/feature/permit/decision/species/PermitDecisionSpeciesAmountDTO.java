package fi.riista.feature.permit.decision.species;

import fi.riista.feature.common.entity.Has2BeginEndDatesDTO;
import fi.riista.feature.gamediary.GameSpecies;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertTrue;
import java.util.Objects;

import static fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount.MAX_SPECIES_AMOUNT;
import static fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount.MIN_SPECIES_AMOUNT;

public class PermitDecisionSpeciesAmountDTO extends Has2BeginEndDatesDTO {
    @Nonnull
    public static PermitDecisionSpeciesAmountDTO create(@Nonnull final PermitDecisionSpeciesAmount speciesAmount,
                                                        final float applicationAmount) {
        return new PermitDecisionSpeciesAmountDTO(speciesAmount, speciesAmount.getGameSpecies(), applicationAmount);
    }

    public PermitDecisionSpeciesAmountDTO() {
    }

    public PermitDecisionSpeciesAmountDTO(
            @Nonnull final PermitDecisionSpeciesAmount speciesAmount,
            @Nonnull final GameSpecies species,
            final float applicationAmount) {
        Objects.requireNonNull(speciesAmount, "speciesAmount must not be null");
        Objects.requireNonNull(species, "species must not be null");
        super.copyDatesFrom(speciesAmount);

        this.id = speciesAmount.getId();
        this.gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        this.amount = speciesAmount.getAmount();
        this.restrictionType = speciesAmount.getRestrictionType();
        this.restrictionAmount = speciesAmount.getRestrictionAmount();
        this.applicationAmount = applicationAmount;
    }

    private Long id;
    private int gameSpeciesCode;
    private float amount;
    private float applicationAmount;
    private PermitDecisionSpeciesAmount.RestrictionType restrictionType;
    private Float restrictionAmount;

    @AssertTrue
    public boolean isValidAmount() {
        return amount >= MIN_SPECIES_AMOUNT && amount < MAX_SPECIES_AMOUNT;
    }

    @AssertTrue
    public boolean isRestrictionAmountValid() {
        return restrictionAmount == null || amount > 0 && restrictionAmount > 0 && restrictionAmount <= amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public float getApplicationAmount() {
        return applicationAmount;
    }

    public void setApplicationAmount(final float applicationAmount) {
        this.applicationAmount = applicationAmount;
    }

    public PermitDecisionSpeciesAmount.RestrictionType getRestrictionType() {
        return restrictionType;
    }

    public void setRestrictionType(PermitDecisionSpeciesAmount.RestrictionType restrictionType) {
        this.restrictionType = restrictionType;
    }

    public Float getRestrictionAmount() {
        return restrictionAmount;
    }

    public void setRestrictionAmount(Float restrictionAmount) {
        this.restrictionAmount = restrictionAmount;
    }
}
