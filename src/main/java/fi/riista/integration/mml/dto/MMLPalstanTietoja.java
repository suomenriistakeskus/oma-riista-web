package fi.riista.integration.mml.dto;

import org.joda.time.LocalDate;

import java.io.Serializable;

/**
 * This class contains parsed fields from GMLv2 WFS response with KTJ data.
 */
public class MMLPalstanTietoja implements Serializable {
    private final String propertyIdentifier;
    private final LocalDate lastUpdateAt;
    private final String gmlGeometry;

    public MMLPalstanTietoja(String propertyIdentifier, LocalDate lastUpdateAt, String gmlGeometry) {
        this.propertyIdentifier = propertyIdentifier;
        this.lastUpdateAt = lastUpdateAt;
        this.gmlGeometry = gmlGeometry;
    }

    public String getPropertyIdentifier() {
        return propertyIdentifier;
    }

    public LocalDate getLastUpdateAt() {
        return lastUpdateAt;
    }

    public String getGmlGeometry() {
        return gmlGeometry;
    }
}
