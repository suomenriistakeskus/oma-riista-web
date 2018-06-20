package fi.riista.feature.gis.zone;

public class GISZoneSizeRhyDTO {
    private final String rhyOfficialCode;
    private final TotalLandWaterSizeDTO bothSize;
    private final TotalLandWaterSizeDTO stateSize;
    private final TotalLandWaterSizeDTO privateSize;

    public GISZoneSizeRhyDTO(final String rhyOfficialCode,
                             final TotalLandWaterSizeDTO bothSize,
                             final TotalLandWaterSizeDTO stateSize,
                             final TotalLandWaterSizeDTO privateSize) {
        this.rhyOfficialCode = rhyOfficialCode;
        this.bothSize = bothSize;
        this.stateSize = stateSize;
        this.privateSize = privateSize;
    }

    public String getRhyOfficialCode() {
        return rhyOfficialCode;
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
