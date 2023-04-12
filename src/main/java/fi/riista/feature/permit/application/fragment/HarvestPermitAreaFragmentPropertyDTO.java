package fi.riista.feature.permit.application.fragment;

import fi.riista.feature.common.entity.PropertyIdentifier;

import static java.util.Objects.requireNonNull;

public class HarvestPermitAreaFragmentPropertyDTO {
    private final String hash;
    private final String propertyNumber;
    private final String propertyName;
    private final double propertyArea;
    private final double propertyWaterArea;
    private final double propertyLandArea;
    private final boolean metsahallitus;

    public HarvestPermitAreaFragmentPropertyDTO(final String hash,
                                                final Long propertyNumberAsLong,
                                                final String propertyName,
                                                final double propertyArea,
                                                final double propertyWaterArea,
                                                final boolean metsahallitus) {
        this.hash = requireNonNull(hash);
        this.propertyNumber = PropertyIdentifier.create(propertyNumberAsLong).getDelimitedValue();
        this.propertyName = propertyName;
        this.propertyArea = propertyArea;
        this.propertyWaterArea = propertyWaterArea;
        this.propertyLandArea = propertyArea - propertyWaterArea;
        this.metsahallitus = metsahallitus;
    }

    public String getHash() {
        return hash;
    }

    public String getPropertyNumber() {
        return propertyNumber;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public double getPropertyArea() {
        return propertyArea;
    }

    public double getPropertyWaterArea() {
        return propertyWaterArea;
    }

    public double getPropertyLandArea() {
        return propertyLandArea;
    }

    public boolean isMetsahallitus() {
        return metsahallitus;
    }
}
