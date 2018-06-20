package fi.riista.integration.mml.support;

import fi.riista.feature.gis.GISPoint;

public class WFS10Filters {
    public static final String SRS_NAME = "EPSG:3067";

    public static String intersects(GISPoint position, String propertyName) {
        return "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\">" +
                "<ogc:Intersects>" +
                "<ogc:PropertyName>" + propertyName + "</ogc:PropertyName>" +
                position.toGmlPoint(SRS_NAME) +
                "</ogc:Intersects></ogc:Filter>";
    }

    public static String dWithin(String propertyName, GISPoint position, int distanceMeters) {
        return "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\">" +
                "<ogc:DWithin>" +
                "<ogc:PropertyName>" + propertyName + "</ogc:PropertyName>" +
                position.toGmlPoint(SRS_NAME) +
                "<ogc:Distance units=\"meter\">" + distanceMeters + "</ogc:Distance >" +
                "</ogc:DWithin></ogc:Filter>";
    }

    public static String bbox(String propertyName, GISPoint lowerCorner, GISPoint upperCorner) {
        return "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\">" +
                "<ogc:BBOX>" +
                "<ogc:PropertyName>" + propertyName + "</ogc:PropertyName>" +
                "<gml:Envelope srsName=\"EPSG:3067\">" +
                "<gml:lowerCorner>" + lowerCorner.toPointString() + "</gml:lowerCorner>" +
                "<gml:upperCorner>" + upperCorner.toPointString() + "</gml:upperCorner>" +
                "</gml:Envelope></ogc:BBOX></ogc:Filter>";
    }

    public static String propertyIsEqual(String propertyName, String propertyValue) {
        return "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">" +
                "<ogc:PropertyIsEqualTo>" +
                "<ogc:PropertyName>" + propertyName + "</ogc:PropertyName>" +
                "<ogc:Literal>" + propertyValue + "</ogc:Literal>" +
                "</ogc:PropertyIsEqualTo>" +
                "</ogc:Filter>";
    }

    private WFS10Filters() {
        throw new AssertionError();
    }
}
