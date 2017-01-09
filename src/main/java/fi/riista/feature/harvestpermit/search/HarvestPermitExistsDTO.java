package fi.riista.feature.harvestpermit.search;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.validation.FinnishHuntingPermitNumber;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import java.util.List;

public class HarvestPermitExistsDTO extends BaseEntityDTO<Long> {

    @Nonnull
    public static HarvestPermitExistsDTO create(@Nonnull final HarvestPermit permit) {
        final HarvestPermitExistsDTO dto = new HarvestPermitExistsDTO();
        DtoUtil.copyBaseFields(permit, dto);
        dto.setPermitType(permit.getPermitType());
        dto.setPermitNumber(permit.getPermitNumber());
        dto.setSpeciesAmounts(F.mapNonNullsToList(permit.getSpeciesAmounts(), HarvestPermitSpeciesAmountDTO::create));
        dto.setHarvestsAsList(permit.isHarvestsAsList());
        dto.setUnavailable(permit.isUnavailable());
        dto.setPermittedMethodAllowed(permit.isPermittedMethodAllowed());
        return dto;
    }

    private Long id;
    private Integer rev;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitType;

    @FinnishHuntingPermitNumber
    private String permitNumber;

    private List<HarvestPermitSpeciesAmountDTO> speciesAmounts;

    private boolean unavailable;
    private boolean harvestsAsList;
    private boolean permittedMethodAllowed;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public String getPermitType() {
        return permitType;
    }

    public void setPermitType(String permitType) {
        this.permitType = permitType;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public List<HarvestPermitSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public void setSpeciesAmounts(List<HarvestPermitSpeciesAmountDTO> speciesAmounts) {
        this.speciesAmounts = speciesAmounts;
    }

    public boolean isUnavailable() {
        return unavailable;
    }

    public void setUnavailable(boolean unavailable) {
        this.unavailable = unavailable;
    }

    public boolean isHarvestsAsList() {
        return harvestsAsList;
    }

    public void setHarvestsAsList(boolean harvestsAsList) {
        this.harvestsAsList = harvestsAsList;
    }

    public boolean isPermittedMethodAllowed() {
        return permittedMethodAllowed;
    }

    public void setPermittedMethodAllowed(boolean permittedMethodAllowed) {
        this.permittedMethodAllowed = permittedMethodAllowed;
    }
}
