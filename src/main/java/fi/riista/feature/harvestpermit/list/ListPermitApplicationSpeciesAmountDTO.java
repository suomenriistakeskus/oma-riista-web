package fi.riista.feature.harvestpermit.list;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ListPermitApplicationSpeciesAmountDTO {
    @Nonnull
    public static ListPermitApplicationSpeciesAmountDTO create(
            @Nonnull final HarvestPermitApplicationSpeciesAmount speciesAmount) {
        return new ListPermitApplicationSpeciesAmountDTO(speciesAmount, speciesAmount.getGameSpecies());
    }

    public ListPermitApplicationSpeciesAmountDTO() {
    }

    public ListPermitApplicationSpeciesAmountDTO(
            @Nonnull final HarvestPermitApplicationSpeciesAmount speciesAmount,
            @Nonnull final GameSpecies species) {
        Objects.requireNonNull(speciesAmount, "speciesAmount must not be null");
        Objects.requireNonNull(species, "species must not be null");

        this.gameSpeciesCode = species.getOfficialCode();
        this.harvestAmount = speciesAmount.getSpecimenAmount();
        this.nestAmount = speciesAmount.getNestAmount();
        this.eggAmount = speciesAmount.getEggAmount();
        this.constructionAmount = speciesAmount.getConstructionAmount();
        this.description = speciesAmount.getMooselikeDescription();
    }
    private int gameSpeciesCode;

    private Float harvestAmount;

    private Integer nestAmount;
    private Integer eggAmount;
    private Integer constructionAmount;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String description;

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public Float getHarvestAmount() {
        return harvestAmount;
    }

    public void setHarvestAmount(final Float harvestAmount) {
        this.harvestAmount = harvestAmount;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
