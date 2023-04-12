package fi.riista.feature.permit.application;

import org.geojson.FeatureCollection;

public class PrintApplicationApproachMapFeatureCollection {
    private final FeatureCollection mapFeatures;
    private final String mapZoom;
    private final FeatureCollection approachMapFeatures;
    private final String approachMapZoom;
    private final String paperSize;
    private final String orientation;

    public PrintApplicationApproachMapFeatureCollection(final FeatureCollection mapFeatures,
                                                        final String mapZoom,
                                                        final FeatureCollection approachMapFeatures,
                                                        final String approachMapZoom,
                                                        final String paperSize,
                                                        final String orientation) {
        this.mapFeatures = mapFeatures;
        this.mapZoom = mapZoom;
        this.approachMapFeatures = approachMapFeatures;
        this.approachMapZoom = approachMapZoom;
        this.paperSize = paperSize;
        this.orientation = orientation;
    }

    public FeatureCollection getMapFeatures() {
        return mapFeatures;
    }

    public String getMapZoom() {
        return mapZoom;
    }

    public FeatureCollection getApproachMapFeatures() {
        return approachMapFeatures;
    }

    public String getApproachMapZoom() {
        return approachMapZoom;
    }

    public String getPaperSize() {
        return paperSize;
    }

    public String getOrientation() {
        return orientation;
    }
}
