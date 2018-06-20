package fi.riista.integration.mml.support;

import org.springframework.util.xml.SimpleNamespaceContext;

import javax.annotation.Nonnull;

public class KTJConstants {
    private static final String NS_URL_WFS = "http://www.opengis.net/wfs";
    private static final String NS_URL_GML = "http://www.opengis.net/gml";
    private static final String NS_URL_OGC = "http://www.opengis.net/ogc";
    private static final String NS_URL_XLINK = "http://www.w3.org/1999/xlink";
    private static final String NS_URL_KTJ_KIINTEISTO = "http://xml.nls.fi/ktjkiiwfs/2010/02";

    public static final String KRJ_PROPERTY_SIJAINTI = "ktjkiiwfs:sijainti";
    public static final String KTJ_PROPERTY_KIINTEISTO_TUNNUS = "ktjkiiwfs:kiinteistotunnus";
    public static final String KTJ_PROPERTY_REK_KIINTEISTOTUNNUS = "ktjkiiwfs:rekisteriyksikonKiinteistotunnus";
    public static final String KRJ_PROPERTY_TEKSTI_KARTALLA = "ktjkiiwfs:tekstiKartalla";
    public static final String KTJ_PROPERTY_KUNTA_TIETO = "ktjkiiwfs:kuntaTieto";
    public static final String KTJ_PROPERTY_PAIVITYSPVM = "ktjkiiwfs:paivityspvm";

    @Nonnull
    public static SimpleNamespaceContext createNamespaceContextForWfs() {
        final SimpleNamespaceContext namespaces = new SimpleNamespaceContext();

        // NOTE: These are the internal prefixes used in XPath expression
        namespaces.bindNamespaceUri("wfs", NS_URL_WFS);
        namespaces.bindNamespaceUri("xlink", NS_URL_XLINK);
        namespaces.bindNamespaceUri("gml", NS_URL_GML);
        namespaces.bindNamespaceUri("ogc", NS_URL_OGC);
        namespaces.bindNamespaceUri("ktjkiiwfs", NS_URL_KTJ_KIINTEISTO);
        namespaces.bindDefaultNamespaceUri(NS_URL_KTJ_KIINTEISTO);

        return namespaces;
    }

}
