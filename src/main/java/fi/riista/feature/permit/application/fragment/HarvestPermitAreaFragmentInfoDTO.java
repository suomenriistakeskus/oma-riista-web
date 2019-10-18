package fi.riista.feature.permit.application.fragment;

import fi.riista.feature.gis.zone.TotalLandWaterSizeDTO;

import java.util.List;

public class HarvestPermitAreaFragmentInfoDTO {
    private final String hash;
    private final TotalLandWaterSizeDTO bothSize;
    private final TotalLandWaterSizeDTO stateSize;
    private final TotalLandWaterSizeDTO privateSize;
    private final List<HarvestPermitAreaFragmentPropertyDTO> propertyNumbers;

    public HarvestPermitAreaFragmentInfoDTO(final HarvestPermitAreaFragmentSizeDTO dto,
                                            final List<HarvestPermitAreaFragmentPropertyDTO> propertyNumbers) {
        this.hash = dto.getHash();
        this.bothSize = dto.getBothSize();
        this.stateSize = dto.getStateSize();
        this.privateSize = dto.getPrivateSize();
        this.propertyNumbers = propertyNumbers;
    }

    public String getHash() {
        return hash;
    }

    public TotalLandWaterSizeDTO getBothSize() {
        return bothSize;
    }

    public TotalLandWaterSizeDTO getStateSize() {
        return stateSize;
    }

    public TotalLandWaterSizeDTO getPrivateSize() {
        return privateSize;
    }

    public List<HarvestPermitAreaFragmentPropertyDTO> getPropertyNumbers() {
        return propertyNumbers;
    }
}
