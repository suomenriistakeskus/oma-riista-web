package fi.riista.feature.permit.application.derogation.damage;

import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

public class DerogationPermitApplicationDamageDTO {
    private int gameSpeciesCode;

    @Min(0)
    @Max(Integer.MAX_VALUE)
    @NotNull
    private Integer causedDamageAmount;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String causedDamageDescription;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String evictionMeasureDescription;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String evictionMeasureEffect;

    public DerogationPermitApplicationDamageDTO() {
    }

    public DerogationPermitApplicationDamageDTO(final HarvestPermitApplicationSpeciesAmount speciesAmount) {
        requireNonNull(speciesAmount, "speciesAmount must not be null");
        requireNonNull(speciesAmount.getGameSpecies(), "species must not be null");

        this.gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        this.causedDamageAmount = speciesAmount.getCausedDamageAmount();
        this.causedDamageDescription = speciesAmount.getCausedDamageDescription();
        this.evictionMeasureDescription = speciesAmount.getEvictionMeasureDescription();
        this.evictionMeasureEffect = speciesAmount.getEvictionMeasureEffect();
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public Integer getCausedDamageAmount() {
        return causedDamageAmount;
    }

    public void setCausedDamageAmount(final Integer causedDamageAmount) {
        this.causedDamageAmount = causedDamageAmount;
    }

    public String getCausedDamageDescription() {
        return causedDamageDescription;
    }

    public void setCausedDamageDescription(final String causedDamageDescription) {
        this.causedDamageDescription = causedDamageDescription;
    }

    public String getEvictionMeasureDescription() {
        return evictionMeasureDescription;
    }

    public void setEvictionMeasureDescription(final String evictionMeasureDescription) {
        this.evictionMeasureDescription = evictionMeasureDescription;
    }

    public String getEvictionMeasureEffect() {
        return evictionMeasureEffect;
    }

    public void setEvictionMeasureEffect(final String evictionMeasureEffect) {
        this.evictionMeasureEffect = evictionMeasureEffect;
    }
}
