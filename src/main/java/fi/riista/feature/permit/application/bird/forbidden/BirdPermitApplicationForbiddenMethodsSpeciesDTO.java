package fi.riista.feature.permit.application.bird.forbidden;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;

public class BirdPermitApplicationForbiddenMethodsSpeciesDTO {

    public BirdPermitApplicationForbiddenMethodsSpeciesDTO() {
    }

    public BirdPermitApplicationForbiddenMethodsSpeciesDTO(final @Nonnull HarvestPermitApplicationSpeciesAmount speciesAmount) {
        this.gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();
        this.justification = speciesAmount.getForbiddenMethodJustification();
        this.forbiddenMethodsUsed = speciesAmount.isForbiddenMethodsUsed();
    }

    private int gameSpeciesCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String justification;

    private Boolean forbiddenMethodsUsed;

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public Boolean isForbiddenMethodsUsed() {
        return forbiddenMethodsUsed;
    }

    public void setForbiddenMethodsUsed(Boolean forbiddenMethodsUsed) {
        this.forbiddenMethodsUsed = forbiddenMethodsUsed;
    }

    // For JSP only
    @JsonIgnore
    public boolean isActive() {
        return Boolean.TRUE.equals(forbiddenMethodsUsed);
    }
}
