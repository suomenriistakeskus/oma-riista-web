package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.validation.FinnishHuntingPermitNumber;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import java.util.List;

public class MobileHarvestPermitExistsDTO extends BaseEntityDTO<Long> {

    public static @Nonnull List<MobileHarvestPermitExistsDTO> create(@Nonnull final List<HarvestPermit> permits) {
        return F.mapNonNullsToList(permits, MobileHarvestPermitExistsDTO::create);
    }

    public static @Nonnull MobileHarvestPermitExistsDTO create(@Nonnull final HarvestPermit permit) {
        MobileHarvestPermitExistsDTO dto = new MobileHarvestPermitExistsDTO();
        DtoUtil.copyBaseFields(permit, dto);
        dto.setPermitType(permit.getPermitType());
        dto.setPermitNumber(permit.getPermitNumber());
        dto.setSpeciesAmounts(MobileHarvestPermitSpeciesAmountDTO.create(permit.getSpeciesAmounts()));
        dto.setUnavailable(permit.isHarvestReportDone());
        return dto;
    }

    private Long id;
    private Integer rev;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitType;

    @FinnishHuntingPermitNumber
    private String permitNumber;

    private boolean unavailable;

    private List<MobileHarvestPermitSpeciesAmountDTO> speciesAmounts;

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

    public List<MobileHarvestPermitSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public void setSpeciesAmounts(List<MobileHarvestPermitSpeciesAmountDTO> speciesAmounts) {
        this.speciesAmounts = speciesAmounts;
    }

    public boolean isUnavailable() {
        return unavailable;
    }

    public void setUnavailable(boolean unavailable) {
        this.unavailable = unavailable;
    }
}
