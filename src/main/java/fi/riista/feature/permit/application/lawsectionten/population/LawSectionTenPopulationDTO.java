package fi.riista.feature.permit.application.lawsectionten.population;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplication;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.SafeHtml;

import javax.persistence.Column;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Arrays;

import static fi.riista.feature.harvestpermit.HarvestPermitCategory.EUROPEAN_BEAVER;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.PARTRIDGE;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class LawSectionTenPopulationDTO {

    @NotNull
    private HarvestPermitCategory category;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotBlank
    private String justification;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotBlank
    private String populationDescription;

    // For european beaver
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String damagesCaused;

    // For partridge
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String transferredAnimalOrigin;
    private Integer transferredAnimalAmount;


    @AssertTrue
    public boolean isValidCategory() {
        return Arrays.asList(EUROPEAN_BEAVER, PARTRIDGE).contains(this.category);
    }

    @AssertTrue
    public boolean isBeaverDataValid() {
        return category != EUROPEAN_BEAVER ||
                (isCommonDataPresent() &&
                        isNotBlank(damagesCaused) &&
                        isBlank(transferredAnimalOrigin) && transferredAnimalAmount == null);

    }

    @AssertTrue
    public boolean isPartridgeDataValid() {
        return category != PARTRIDGE ||
                (isCommonDataPresent() &&
                        isNotBlank(transferredAnimalOrigin) &&
                        transferredAnimalAmount != null &&
                        transferredAnimalAmount >= 0 &&
                        isBlank(damagesCaused));
    }

    private boolean isCommonDataPresent() {
        return isNotBlank(populationDescription) && isNotBlank(justification);
    }

    public static LawSectionTenPopulationDTO createFrom(final HarvestPermitApplication application,
                                                        final LawSectionTenPermitApplication sectionTenApplication) {
        return new LawSectionTenPopulationDTO(application.getHarvestPermitCategory(),
                sectionTenApplication.getJustification(), sectionTenApplication.getPopulationDescription(),
                sectionTenApplication.getDamagesCaused(), sectionTenApplication.getTransferredAnimalOrigin(),
                sectionTenApplication.getTransferredAnimalAmount());
    }

    // For jackson
    public LawSectionTenPopulationDTO() {
    }

    public LawSectionTenPopulationDTO(final HarvestPermitCategory category, final String justification,
                                      final String populationDescription, final String damagesCaused,
                                      final String transferredAnimalOrigin, final Integer transferredAnimalAmount) {
        this.category = category;
        this.justification = justification;
        this.populationDescription = populationDescription;
        this.damagesCaused = damagesCaused;
        this.transferredAnimalOrigin = transferredAnimalOrigin;
        this.transferredAnimalAmount = transferredAnimalAmount;
    }

    public HarvestPermitCategory getCategory() {
        return category;
    }

    public void setCategory(final HarvestPermitCategory category) {
        this.category = category;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }

    public String getPopulationDescription() {
        return populationDescription;
    }

    public void setPopulationDescription(final String populationDescription) {
        this.populationDescription = populationDescription;
    }

    public String getDamagesCaused() {
        return damagesCaused;
    }

    public void setDamagesCaused(final String damagesCaused) {
        this.damagesCaused = damagesCaused;
    }

    public String getTransferredAnimalOrigin() {
        return transferredAnimalOrigin;
    }

    public void setTransferredAnimalOrigin(final String transferredAnimalOrigin) {
        this.transferredAnimalOrigin = transferredAnimalOrigin;
    }

    public Integer getTransferredAnimalAmount() {
        return transferredAnimalAmount;
    }

    public void setTransferredAnimalAmount(final Integer transferredAnimalAmount) {
        this.transferredAnimalAmount = transferredAnimalAmount;
    }
}
