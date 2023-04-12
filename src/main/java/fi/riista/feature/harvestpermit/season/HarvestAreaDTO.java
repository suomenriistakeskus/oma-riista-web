package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.util.DtoUtil;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;

public class HarvestAreaDTO extends BaseEntityDTO<Long> {

    @Nonnull
    public static HarvestAreaDTO create(@Nonnull final HarvestArea harvestArea) {
        final HarvestAreaDTO dto = new HarvestAreaDTO();
        DtoUtil.copyBaseFields(harvestArea, dto);
        dto.setNameFI(harvestArea.getNameFinnish());
        dto.setNameSV(harvestArea.getNameSwedish());
        dto.setHarvestAreaType(harvestArea.getType());
        dto.setOfficialCode(harvestArea.getOfficialCode());
        return dto;
    }

    private Long id;
    private Integer rev;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameFI;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameSV;

    private HarvestArea.HarvestAreaType harvestAreaType;
    private String officialCode;

    public HarvestAreaDTO() {
    }

    public HarvestAreaDTO(@Nonnull final HarvestAreaDTO other) {
        super(other);
        setNameFI(other.getNameFI());
        setNameSV(other.getNameSV());
    }

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

    public void setNameFI(String nameFI) {
        this.nameFI = nameFI;
    }

    public String getNameFI() {
        return nameFI;
    }

    public void setNameSV(String nameSV) {
        this.nameSV = nameSV;
    }

    public String getNameSV() {
        return nameSV;
    }

    public HarvestArea.HarvestAreaType getHarvestAreaType() {
        return harvestAreaType;
    }

    public void setHarvestAreaType(final HarvestArea.HarvestAreaType harvestAreaType) {
        this.harvestAreaType = harvestAreaType;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public void setOfficialCode(final String officialCode) {
        this.officialCode = officialCode;
    }
}
