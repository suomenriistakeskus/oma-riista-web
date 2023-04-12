package fi.riista.feature.permit.application.fragment;

import fi.riista.feature.permit.application.PrintApplicationApproachMapFeatureCollection;

public class PrintApplicationAreaFragmentMapModel {

    final PrintApplicationApproachMapFeatureCollection featureCollection;
    final String filename;

    public PrintApplicationAreaFragmentMapModel(final PrintApplicationApproachMapFeatureCollection featureCollection, final String filename) {
        this.featureCollection = featureCollection;
        this.filename = filename;
    }

    public PrintApplicationApproachMapFeatureCollection getFeatureCollection() {
        return featureCollection;
    }

    public String getFilename() {
        return filename;
    }
}
