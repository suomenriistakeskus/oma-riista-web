package fi.riista.integration.mml.support;

import fi.riista.feature.gis.GISPoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class WFS20Filters {
    public static final String SRS_NAME = "urn:ogc:def:crs:EPSG::3067";

    private static final String NS_URL_FES = "http://www.opengis.net/fes/2.0";
    private static final String NS_URL_GML = "http://www.opengis.net/gml/3.2";

    private static final String SCHEMA_LOCATION_FES = "http://schemas.opengis.net/filter/2.0.0/filterAll.xsd";
    private static final String SCHEMA_LOCATION_GML = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";

    private static final String IMPORTED_SCHEMAS = NS_URL_FES + " " + SCHEMA_LOCATION_FES + " "
            + NS_URL_GML + " " + SCHEMA_LOCATION_GML;

    public static String dWithin(final String propertyName, final GISPoint position, final int distanceMeters) {
        final Document document = createDocument();
        final Element point = createPointElement(position, document);
        final Element distance = createDistanceElement(distanceMeters, document);
        final Element valueReference = document.createElementNS(NS_URL_FES, "fes:ValueReference");
        final Element dWithin = document.createElementNS(NS_URL_FES, "fes:DWithin");
        final Element filter = document.createElementNS(NS_URL_FES, "fes:Filter");

        valueReference.setTextContent(propertyName);
        dWithin.appendChild(valueReference);
        dWithin.appendChild(point);
        dWithin.appendChild(distance);
        filter.appendChild(dWithin);

        setImportedSchemas(filter, IMPORTED_SCHEMAS);

        document.appendChild(filter);

        return WFSDomUtil.domToString(document, true, true);
    }

    @Nonnull
    private static Element createDistanceElement(final int distanceMeters, final Document document) {
        final Element distance = document.createElementNS(NS_URL_FES, "fes:Distance");
        distance.setAttribute("uom", "m");
        distance.setTextContent(Integer.toString(distanceMeters));
        return distance;
    }

    @Nonnull
    private static Element createPointElement(final GISPoint position, final Document document) {
        final Element pos = document.createElementNS(NS_URL_GML, "gml:pos");
        pos.setTextContent(position.toPointString());

        final Element point = document.createElementNS(NS_URL_GML, "gml:Point");
        point.setAttribute("srsName", SRS_NAME);
        point.appendChild(pos);

        return point;
    }

    private static void setImportedSchemas(final Element filter, final String importedSchemas) {
        filter.setAttributeNS(
                "http://www.w3.org/2000/xmlns/",
                "xmlns:xsi",
                "http://www.w3.org/2001/XMLSchema-instance");
        filter.setAttributeNS(
                "http://www.w3.org/2001/XMLSchema-instance",
                "xsi:schemaLocation",
                importedSchemas);
    }

    private static Document createDocument() {
        try {
            final DocumentBuilderFactory dbf = createDocumentBuilderFactory();
            final DocumentBuilder builder = dbf.newDocumentBuilder();
            return builder.newDocument();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static DocumentBuilderFactory createDocumentBuilderFactory() {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setIgnoringComments(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        return documentBuilderFactory;
    }
}
