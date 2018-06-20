package fi.riista.integration.mml.support;

import org.springframework.util.xml.SimpleNamespaceContext;

import javax.annotation.Nonnull;

public class InspireWFSConstants {
    private static final String NS_URL_WFS = "http://www.opengis.net/wfs/2.0";
    private static final String NS_URL_FES = "http://www.opengis.net/fes/2.0";
    private static final String NS_URL_GML = "http://www.opengis.net/gml/3.2";
    private static final String NS_URL_XLINK = "http://www.w3.org/1999/xlink";
    private static final String NS_URL_BASE = "http://inspire.ec.europa.eu/schemas/base/3.3";
    private static final String NS_URL_BU_BASE = "http://inspire.ec.europa.eu/schemas/bu-base/4.0";
    private static final String NS_URL_BU_CORE2D = "http://inspire.ec.europa.eu/schemas/bu-core2d/4.0";

    @Nonnull
    public static SimpleNamespaceContext createNamespaceContextForWfs() {
        final SimpleNamespaceContext namespaces = new SimpleNamespaceContext();

        // NOTE: These are the internal prefixes used in XPath expression
        namespaces.bindNamespaceUri("wfs", NS_URL_WFS);
        namespaces.bindNamespaceUri("xlink", NS_URL_XLINK);
        namespaces.bindNamespaceUri("gml", NS_URL_GML);
        namespaces.bindNamespaceUri("fes", NS_URL_FES);
        namespaces.bindNamespaceUri("base", NS_URL_BASE);
        namespaces.bindNamespaceUri("bu:base", NS_URL_BU_BASE);
        namespaces.bindNamespaceUri("bu:core2d", NS_URL_BU_CORE2D);
        namespaces.bindDefaultNamespaceUri(NS_URL_WFS);

        return namespaces;
    }

}
