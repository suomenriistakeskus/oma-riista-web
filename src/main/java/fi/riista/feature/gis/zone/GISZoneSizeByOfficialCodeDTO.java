package fi.riista.feature.gis.zone;

public class GISZoneSizeByOfficialCodeDTO {
    private final String officialCode;
    private final TotalLandWaterSizeDTO bothSize;
    private final TotalLandWaterSizeDTO stateSize;
    private final TotalLandWaterSizeDTO privateSize;

    public GISZoneSizeByOfficialCodeDTO(final String officialCode,
                                        final TotalLandWaterSizeDTO bothSize,
                                        final TotalLandWaterSizeDTO stateSize,
                                        final TotalLandWaterSizeDTO privateSize) {
        this.officialCode = officialCode;
        this.bothSize = bothSize;
        this.stateSize = stateSize;
        this.privateSize = privateSize;
    }

    public String getOfficialCode() {
        return officialCode;
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
}
