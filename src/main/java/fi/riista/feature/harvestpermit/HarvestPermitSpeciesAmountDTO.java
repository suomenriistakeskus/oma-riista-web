package fi.riista.feature.harvestpermit;

import fi.riista.feature.common.entity.Has2BeginEndDatesDTO;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.GameSpecies;

import javax.annotation.Nonnull;
import java.util.Objects;

public class HarvestPermitSpeciesAmountDTO extends Has2BeginEndDatesDTO {

    @Nonnull
    public static HarvestPermitSpeciesAmountDTO create(
            @Nonnull final HarvestPermitSpeciesAmount speciesAmount) {
        return create(speciesAmount, speciesAmount.getGameSpecies());
    }

    @Nonnull
    public static HarvestPermitSpeciesAmountDTO create(
            @Nonnull final HarvestPermitSpeciesAmount speciesAmount,
            @Nonnull final GameSpecies species) {
        Objects.requireNonNull(speciesAmount, "speciesAmount must not be null");
        Objects.requireNonNull(species, "species must not be null");

        final HarvestPermitSpeciesAmountDTO dto = new HarvestPermitSpeciesAmountDTO();
        dto.setId(speciesAmount.getId());
        dto.setRev(speciesAmount.getConsistencyVersion());
        dto.copyDatesFrom(speciesAmount);
        dto.setGameSpecies(GameSpeciesDTO.create(species));
        dto.setAmount(speciesAmount.getAmount());
        dto.setRestrictionType(speciesAmount.getRestrictionType());
        dto.setRestrictionAmount(speciesAmount.getRestrictionAmount());

        return dto;
    }

    private Long id;
    private Integer rev;
    private GameSpeciesDTO gameSpecies;
    private float amount;
    private HarvestPermitSpeciesAmount.RestrictionType restrictionType;
    private Float restrictionAmount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRev() {
        return rev;
    }

    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public GameSpeciesDTO getGameSpecies() {
        return gameSpecies;
    }

    public void setGameSpecies(GameSpeciesDTO gameSpecies) {
        this.gameSpecies = gameSpecies;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public HarvestPermitSpeciesAmount.RestrictionType getRestrictionType() {
        return restrictionType;
    }

    public void setRestrictionType(HarvestPermitSpeciesAmount.RestrictionType restrictionType) {
        this.restrictionType = restrictionType;
    }

    public Float getRestrictionAmount() {
        return restrictionAmount;
    }

    public void setRestrictionAmount(Float restrictionAmount) {
        this.restrictionAmount = restrictionAmount;
    }

}
