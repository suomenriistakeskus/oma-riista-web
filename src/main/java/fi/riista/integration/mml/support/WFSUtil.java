package fi.riista.integration.mml.support;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.gis.GISPoint;
import org.apache.commons.io.output.StringBuilderWriter;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.Set;

public class WFSUtil {
    public static final String NS_URL_WFS = "http://www.opengis.net/wfs";
    public static final String NS_URL_GML = "http://www.opengis.net/gml";
    public static final String NS_URL_OGC = "http://www.opengis.net/ogc";
    public static final String NS_URL_XLINK = "http://www.w3.org/1999/xlink";
    public static final String NS_URL_KTJ_KIINTEISTO = "http://xml.nls.fi/ktjkiiwfs/2010/02";
    public static final String SRS_NAME = "EPSG:3067";

    public static final DocumentBuilderFactory DOC_BUILDER_FACTORY;

    static {
        DOC_BUILDER_FACTORY = createDocumentBuilderFactory();
    }

    public static Document parse(final InputStream inputStream) throws Exception {
        DocumentBuilder documentBuilder = DOC_BUILDER_FACTORY.newDocumentBuilder();
        return documentBuilder.parse(inputStream);
    }

    public static String intersects(GISPoint position, String propertyName) {
        return "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\">" +
                "<ogc:Intersects>" +
                "<ogc:PropertyName>" + propertyName + "</ogc:PropertyName>" +
                position.toGmlPoint() +
                "</ogc:Intersects></ogc:Filter>";
    }

    public static String dWithin(String propertyName, GISPoint position, int distanceMeters) {
        return "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\">" +
                "<ogc:DWithin>" +
                "<ogc:PropertyName>" + propertyName + "</ogc:PropertyName>" +
                position.toGmlPoint() +
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

    public static Set<String> collectElementTextToStringSet(final NodeList nodeList) {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);

            if (StringUtils.hasText(item.getTextContent())) {
                builder.add(item.getTextContent());
            }
        }

        return builder.build();
    }

    private static DocumentBuilderFactory createDocumentBuilderFactory() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setIgnoringComments(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        return documentBuilderFactory;
    }

    public static XPathExpression createXPathExpression(String expression) {
        SimpleNamespaceContext namespaces = new SimpleNamespaceContext();

        // NOTE: These are the internal prefixes used in XPath expression
        namespaces.bindNamespaceUri("wfs", NS_URL_WFS);
        namespaces.bindNamespaceUri("xlink", NS_URL_XLINK);
        namespaces.bindNamespaceUri("gml", NS_URL_GML);
        namespaces.bindNamespaceUri("ogc", NS_URL_OGC);
        namespaces.bindNamespaceUri("ktjkiiwfs", NS_URL_KTJ_KIINTEISTO);
        namespaces.bindDefaultNamespaceUri(NS_URL_KTJ_KIINTEISTO);

        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(namespaces);

        try {
            return xpath.compile(expression);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    public static String domToString(final Node doc,
                                     final boolean pretty,
                                     final boolean omitXml) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXml ? "yes" : "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, pretty ? "yes" : "no");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            if (pretty) {
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            }

            try (StringBuilderWriter writer = new StringBuilderWriter()) {
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                return writer.getBuilder().toString();
            }
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public static NodeList getNodeSet(final XPathExpression expression,
                                      final Node node) {
        final NodeList nodeList;
        try {
            nodeList = (NodeList) expression.evaluate(node, XPathConstants.NODESET);

        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        return nodeList;
    }

    public static Node getSingleNode(final XPathExpression expression,
                                     final Node node) {
        try {
            return (Node) expression.evaluate(node, XPathConstants.NODE);

        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getString(final XPathExpression expression,
                                   final Node node) {
        try {
            return (String) expression.evaluate(node, XPathConstants.STRING);

        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private WFSUtil() {
        throw new AssertionError();
    }
}
