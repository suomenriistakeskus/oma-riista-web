package fi.riista.feature.permit.area.rhy;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.gis.zone.TotalLandWaterSizeDTO;
import fi.riista.feature.organization.OrganisationNameDTO;

public class HarvestPermitAreaRhyDTO extends BaseEntityDTO<Long> {
    public static HarvestPermitAreaRhyDTO create(HarvestPermitAreaRhy permitAreaRhy) {
        final OrganisationNameDTO rhyDto = OrganisationNameDTO.create(permitAreaRhy.getRhy());

        final HarvestPermitAreaRhyDTO dto = new HarvestPermitAreaRhyDTO();
        dto.setId(permitAreaRhy.getId());
        dto.setRev(permitAreaRhy.getConsistencyVersion());
        dto.setRhy(rhyDto);
        dto.setBothSize(new TotalLandWaterSizeDTO(permitAreaRhy.getAreaSize(), permitAreaRhy.getLandSize(), permitAreaRhy.getWaterSize()));
        dto.setStateSize(new TotalLandWaterSizeDTO(permitAreaRhy.getStateSize(), permitAreaRhy.getStateLandSize(), permitAreaRhy.getStateWaterSize()));
        dto.setPrivateSize(new TotalLandWaterSizeDTO(permitAreaRhy.getPrivateSize(), permitAreaRhy.getPrivateLandSize(), permitAreaRhy.getPrivateWaterSize()));
        return dto;
    }

    private Long id;
    private Integer rev;

    private OrganisationNameDTO rhy;
    private TotalLandWaterSizeDTO bothSize;
    private TotalLandWaterSizeDTO stateSize;
    private TotalLandWaterSizeDTO privateSize;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public OrganisationNameDTO getRhy() {
        return rhy;
    }

    public void setRhy(OrganisationNameDTO rhy) {
        this.rhy = rhy;
    }

    public TotalLandWaterSizeDTO getBothSize() {
        return bothSize;
    }

    public void setBothSize(final TotalLandWaterSizeDTO bothSize) {
        this.bothSize = bothSize;
    }

    public TotalLandWaterSizeDTO getStateSize() {
        return stateSize;
    }

    public void setStateSize(final TotalLandWaterSizeDTO stateSize) {
        this.stateSize = stateSize;
    }

    public TotalLandWaterSizeDTO getPrivateSize() {
        return privateSize;
    }

    public void setPrivateSize(final TotalLandWaterSizeDTO privateSize) {
        this.privateSize = privateSize;
    }
}
