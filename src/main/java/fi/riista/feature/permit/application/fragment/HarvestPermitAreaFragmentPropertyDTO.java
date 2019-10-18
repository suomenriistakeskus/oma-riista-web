package fi.riista.feature.permit.application.fragment;

import fi.riista.feature.common.entity.PropertyIdentifier;

import static java.util.Objects.requireNonNull;

public class HarvestPermitAreaFragmentPropertyDTO {
    private final String hash;
    private final String propertyNumber;
    private final String propertyName;
    private final double propertyArea;
    private final boolean metsahallitus;

    public HarvestPermitAreaFragmentPropertyDTO(final String hash,
                                                final Long propertyNumberAsLong,
                                                final String propertyName,
                                                final double propertyArea,
                                                final boolean metsahallitus) {
        this.hash = requireNonNull(hash);
        this.propertyNumber = PropertyIdentifier.create(propertyNumberAsLong).getDelimitedValue();
        this.propertyName = propertyName;
        this.propertyArea = propertyArea;
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

    public boolean isMetsahallitus() {
        return metsahallitus;
    }
}
