package fi.riista.feature.permit.application.fragment;

public class HarvestPermitAreaFragmentInfoDTO {
    private final String hash;
    private final Double areaSize;
    private final Double waterAreaSize;
    private final Double valtionmaaAreaSize;
    private final Double valtionmaaWaterAreaSize;
    private final String propertyNumber;
    private final Double propertyArea;
    private final Integer propertyId;
    private boolean metsahallitus;

    public HarvestPermitAreaFragmentInfoDTO(final String hash,
                                            final Double areaSize, final Double waterAreaSize,
                                            final Double valtionmaaAreaSize, final Double valtionmaaWaterAreaSize,
                                            final String propertyNumber, final Double propertyArea,
                                            final Integer propertyId) {
        this.hash = hash;
        this.areaSize = areaSize;
        this.waterAreaSize = waterAreaSize;
        this.valtionmaaAreaSize = valtionmaaAreaSize;
        this.valtionmaaWaterAreaSize = valtionmaaWaterAreaSize;
        this.propertyNumber = propertyNumber;
        this.propertyArea = propertyArea;
        this.propertyId = propertyId;
    }

    public String getHash() {
        return hash;
    }

    public Double getAreaSize() {
        return areaSize;
    }

    public Double getWaterAreaSize() {
        return waterAreaSize;
    }

    public Double getValtionmaaAreaSize() {
        return valtionmaaAreaSize;
    }

    public Double getValtionmaaWaterAreaSize() {
        return valtionmaaWaterAreaSize;
    }

    public String getPropertyNumber() {
        return propertyNumber;
    }

    public Double getPropertyArea() {
        return propertyArea;
    }

    public Integer getPropertyId() {
        return propertyId;
    }

    public boolean isMetsahallitus() {
        return metsahallitus;
    }

    public void setMetsahallitus(final boolean metsahallitus) {
        this.metsahallitus = metsahallitus;
    }
}
