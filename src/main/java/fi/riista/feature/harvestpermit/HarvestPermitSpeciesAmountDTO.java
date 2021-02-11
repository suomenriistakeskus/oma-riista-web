package fi.riista.feature.harvestpermit;

import fi.riista.feature.common.dto.Has2BeginEndDatesDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesDTO;

import javax.annotation.Nonnull;
import java.util.Objects;

public class HarvestPermitSpeciesAmountDTO extends Has2BeginEndDatesDTO {

    @Nonnull
    public static HarvestPermitSpeciesAmountDTO create(@Nonnull final HarvestPermitSpeciesAmount speciesAmount) {
        return new HarvestPermitSpeciesAmountDTO(speciesAmount, speciesAmount.getGameSpecies());
    }

    @Nonnull
    public static HarvestPermitSpeciesAmountDTO create(@Nonnull final HarvestPermitSpeciesAmount speciesAmount,
                                                       @Nonnull final GameSpecies species) {
        return new HarvestPermitSpeciesAmountDTO(speciesAmount, species);
    }

    public HarvestPermitSpeciesAmountDTO() {
    }

    public HarvestPermitSpeciesAmountDTO(
            @Nonnull final HarvestPermitSpeciesAmount speciesAmount,
            @Nonnull final GameSpecies species) {
        Objects.requireNonNull(speciesAmount, "speciesAmount must not be null");
        Objects.requireNonNull(species, "species must not be null");
        super.copyDatesFrom(speciesAmount);

        this.id = speciesAmount.getId();
        this.rev = speciesAmount.getConsistencyVersion();
        this.gameSpecies = GameSpeciesDTO.create(species);
        this.amount = speciesAmount.getSpecimenAmount();
        this.nestAmount = speciesAmount.getNestAmount();
        this.eggAmount = speciesAmount.getEggAmount();
        this.constructionAmount = speciesAmount.getConstructionAmount();
        this.restrictionType = speciesAmount.getRestrictionType();
        this.restrictionAmount = speciesAmount.getRestrictionAmount();
    }

    private Long id;
    private Integer rev;
    private GameSpeciesDTO gameSpecies;
    private Float amount;
    private Integer nestAmount;
    private Integer eggAmount;
    private Integer constructionAmount;
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

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
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
