package fi.riista.feature.permit.application.fragment;

public class PropertyNumberAreaSizeDTO {

    private String propertyIdentifier;
    private Double area;
    private boolean metsahallitus;

    public PropertyNumberAreaSizeDTO(final String propertyIdentifier, final Double area, final boolean metsahallitus) {
        this.propertyIdentifier = propertyIdentifier;
        this.area = area;
        this.metsahallitus = metsahallitus;
    }

    public String getPropertyIdentifier() {
        return propertyIdentifier;
    }

    public void setPropertyIdentifier(String propertyIdentifier) {
        this.propertyIdentifier = propertyIdentifier;
    }

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public boolean isMetsahallitus() {
        return metsahallitus;
    }

    public void setMetsahallitus(final boolean metsahallitus) {
        this.metsahallitus = metsahallitus;
    }
}
